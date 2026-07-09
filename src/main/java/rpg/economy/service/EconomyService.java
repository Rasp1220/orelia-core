package rpg.economy.service;

import rpg.economy.repository.EconomyRepository;

import java.util.UUID;

/**
 * Public balance API for the rest of the plugin (quest rewards, NPC shops, weapon sale,
 * enhancement cost). {@link rpg.economy.vault.OreliaVaultEconomy} is a thin adapter over
 * this same service for external Vault-dependent plugins.
 */
public final class EconomyService {

    private final EconomyRepository repository;

    public EconomyService(EconomyRepository repository) {
        this.repository = repository;
    }

    public double getBalance(UUID uuid) {
        return repository.getBalance(uuid);
    }

    public boolean has(UUID uuid, double amount) {
        return repository.getBalance(uuid) >= amount;
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) {
            return;
        }
        repository.setBalance(uuid, repository.getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) {
            return true;
        }
        double balance = repository.getBalance(uuid);
        if (balance < amount) {
            return false;
        }
        repository.setBalance(uuid, balance - amount);
        return true;
    }

    public void setBalance(UUID uuid, double amount) {
        repository.setBalance(uuid, amount);
    }
}
