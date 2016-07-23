package io.github.leonawicz.inventory;

import android.provider.BaseColumns;

public class InvContract {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "io.github.leonawicz.inventory.invdb";

    InvContract() {
    }

    public static abstract class InvEntry implements BaseColumns {
        public static final String TBLNAME = "entry";
        public static final String COLNAME_ITEM = "part";
        public static final String COLNAME_SUP = "supplier";
        public static final String COLNAME_QTY = "quantity";
        public static final String COLNAME_COST = "cost";
        public static final String COLNAME_PRICE = "price";
        public static final String COLNAME_BITMAP = "bitmap";
    }
}
