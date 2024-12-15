package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.utils.ui.CustomInventory;

public class DefalutUi extends CustomInventory {
    @Override
    public void setup() {

    }

    @Override
    public String getTitle() {
        return "§7Menu de configuration";
    }

    @Override
    public int getLines() {
        return 0;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
