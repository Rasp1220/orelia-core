# ダメージ計算式

orelia-coreの戦闘ダメージ計算は、`rpg.status.combat.DamageFormula`(純粋な計算ユーティリティ、単体テスト付き)を共通の基盤として、複数のリスナーが順番に適用していく方式です。このドキュメントでは、実際に何がどの順番で計算されているかを実例とともに説明します。

## 計算の全体像

```
基礎ダメージ算出 → クリティカル判定 → 攻撃側ATK%加算 → 防御側DEF軽減 → 属性弱点倍率
```

## 1. 基礎ダメージ

攻撃手段によって、基礎ダメージの出どころが異なります。

| 攻撃手段 | 基礎ダメージ | 該当コード |
|---|---|---|
| プレイヤーの武器攻撃 | `武器の攻撃力 × 強化倍率` | `WeaponUseListener` |
| スキル攻撃 | `武器の攻撃力 × 強化倍率 × スキルのレベル別倍率` | `SkillDamage.baseDamage()` |
| モンスターの攻撃 | `モンスターの攻撃力`(`monsters.yml`の`attack-power`) | `MonsterCombatListener` |

## 2. クリティカル判定

クリティカル判定はダメージ確定の**直後**、防御側の計算より前に行われます。

- **判定確率** = 武器/モンスター自身の`crit-rate` + 攻撃者の`CRT`ステータス(プレイヤーのみ加算、モンスターは自身の`crit-rate`のみ)
- **命中したときの倍率** = `武器/モンスター自身のcrit-multiplier + 攻撃者のCRT_DMGステータス ÷ 100`(プレイヤーのみ加算)

```java
// DamageFormula.java
public static boolean rollCrit(double critRatePercent) {
    return MathUtil.rollChance(critRatePercent);
}
public static double criticalMultiplier(double baseCritMultiplier, double critDmgPercent) {
    return baseCritMultiplier + critDmgPercent / 100.0;
}
```

例: 武器の`crit-multiplier: 1.5`、プレイヤーの`CRT_DMG: 20`のとき → `1.5 + 20/100 = 1.7倍`。

## 3. 攻撃側ATK%加算(プレイヤーのみ)

攻撃者がプレイヤーの場合のみ、`ATK`ステータスがパーセンテージボーナスとして乗算されます。モンスターの攻撃力には適用されません(モンスターは`monsters.yml`の`attack-power`で完結)。

```java
public static double applyAttackBonus(double damage, double atkPercent) {
    return damage * (1 + atkPercent / 100.0);
}
```

例: `ATK: 10`のとき → `damage × 1.10`。

## 4. 防御側DEF軽減

被弾側の防御力(プレイヤーなら`DEF`ステータス、モンスターなら`monsters.yml`の`defense`)が、同じ曲線で軽減を計算します。

```java
public static double mitigate(double damage, double defense) {
    return damage * (1 - defense / (defense + 100.0));
}
```

`defense = 100`で50%軽減、`defense = 0`で軽減なし、という緩やかな逓減曲線です。防御力がどれだけ高くても100%軽減にはなりません。

## 5. 属性弱点倍率(モンスターが被弾側の場合のみ)

攻撃者が装備している武器の属性が、モンスターの`weakness`(`monsters.yml`)と一致する場合、固定で**×1.5**が乗算されます(`MonsterCombatListener.WEAKNESS_MULTIPLIER`)。プレイヤーが被弾側の場合、属性弱点の概念は現状ありません。

## 実例

**条件**: プレイヤー(見習いの剣、`attack-power: 4.0` `crit-rate: 5.0` `crit-multiplier: 1.5`、強化倍率1.0)が、`森のスライム`(`defense: 0` `weakness: FIRE`、装備武器は無属性のため弱点不一致)を攻撃。プレイヤーの最終ステータスは`ATK: 10` `CRT: 5` `CRT_DMG: 20`。

### 通常時(クリティカル不発生)

| ステップ | 計算 | 結果 |
|---|---|---|
| 基礎ダメージ | `4.0 × 1.0` | 4.0 |
| クリティカル判定 | 確率 `5 + 5 = 10%` → 不発生 | 4.0(変化なし) |
| 属性弱点 | 不一致 | 4.0(変化なし) |
| 防御軽減 | `4.0 × (1 - 0/(0+100))` | 4.0 |
| ATK%加算 | `4.0 × (1 + 10/100)` | **4.4** |

### クリティカル発生時

| ステップ | 計算 | 結果 |
|---|---|---|
| 基礎ダメージ | `4.0 × 1.0` | 4.0 |
| クリティカル判定 | 確率10%で発生、倍率 `1.5 + 20/100 = 1.7` | `4.0 × 1.7 = 6.8` |
| 属性弱点 | 不一致 | 6.8(変化なし) |
| 防御軽減 | `6.8 × (1 - 0/(0+100))` | 6.8 |
| ATK%加算 | `6.8 × (1 + 10/100)` | **7.48** |

## 実装上の注意

- 計算式の実体は`rpg/status/combat/DamageFormula.java`の4メソッド(`mitigate`/`applyAttackBonus`/`criticalMultiplier`/`rollCrit`)に集約されている。計算式を変更する場合はここを直す(各リスナーに直接式を書かない)。
- `DamageFormula.CRIT_METADATA_KEY`は、クリティカルが発生した攻撃側エンティティに一時的に立てるBukkit metadataキー。ダメージ数値表示(`DamageDisplayListener`)がこれを読んで色・サイズを変える。
- `rpg/test/java/rpg/status/combat/DamageFormulaTest.java`に、乱数を含まない部分(`mitigate`/`applyAttackBonus`/`criticalMultiplier`)の単体テストがある。
