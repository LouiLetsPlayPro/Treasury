/*
 * Copyright (c) 2021 lokka30.
 * This code is part of Treasury, an Economy API for Minecraft servers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You have received a copy of the GNU Affero General Public License
 * with this program - please see the LICENSE.md file. Alternatively,
 * please visit the <https://www.gnu.org/licenses/> website.
 *
 * Please see <https://github.com/lokka30/Treasury> for more information on this resource.
 */

package me.lokka30.treasury.api.account;

import java.util.List;
import java.util.UUID;

/**
 * @author lokka30
 * @since v1.0.0
 * @see Account
 * TODO A description about what a Bank Account is in Treasury.
 */
@SuppressWarnings("unused")
public interface BankAccount extends Account {

    /**
     * @author lokka30
     * @since v1.0.0
     * Bank accounts are owned by players. The owner
     * of a bankcan be changed at any point.
     * @return the UUID of the bank's owning player.
     */
    UUID getOwningPlayerId();

    /**
     * @author lokka30
     * @since v1.0.0
     * Checks if a player owns the bank.
     * @param uuid of the player to check.
     * @return whether the player with specified UUID owns the bank.
     */
    default boolean isBankOwner(UUID uuid) {
        return getOwningPlayerId() == uuid;
    }

    /**
     * @author lokka30
     * @since v1.0.0
     * Get a list of each UUID of each member of this bank.
     * @return the list of UUIDs.
     */
    List<UUID> getBankMembersIds();

    /**
     * @author lokka30
     * @since v1.0.0
     * Check if the specified player is a member of the banks.
     * @param uuid of the player to check.
     * @return whether the player is a member of the bank.
     */
    default boolean isBankMember(UUID uuid) {
        return getBankMembersIds().contains(uuid);
    }

    /**
     * @author lokka30
     * @since v1.0.0
     * Makes a player a member of the bank.
     * @param uuid of the player to make a member of the bank.
     */
    void addBankMember(UUID uuid);

    /**
     * @author lokka30
     * @since v1.0.0
     * Makes a player no longer a member of the bank.
     * @param uuid of the player to remove the member status of in the bank.
     */
    void removeBankMember(UUID uuid);
}
