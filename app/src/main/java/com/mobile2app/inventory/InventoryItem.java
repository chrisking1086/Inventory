package com.mobile2app.inventory;

import java.util.Objects;

public class InventoryItem {
    private final String itemName;
    private final String itemDescription;
    private final int itemQuantity;

    public InventoryItem(String itemName, String itemDescription, int itemQuantity) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemQuantity = itemQuantity;
    }

    // Getters for the fields
    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", itemQuantity=" + itemQuantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return itemQuantity == that.itemQuantity &&
                Objects.equals(itemName, that.itemName) &&
                Objects.equals(itemDescription, that.itemDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemName, itemDescription, itemQuantity);
    }
}