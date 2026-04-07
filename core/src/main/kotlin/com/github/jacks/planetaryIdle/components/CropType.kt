package com.github.jacks.planetaryIdle.components

import java.math.BigDecimal

data class CropType(
    val cropName: String,
    val color: String,
    val tier: Int,
    val baseValue: BigDecimal,
    val baseProductionTime: Float,
) {
    /** Unique stable ID used for persistence. */
    val id: String get() = "${color}_${cropName.lowercase().replace(" ", "_")}"
}

object CropRegistry {
    val all: List<CropType> = listOf(
        // ── RED ──────────────────────────────────────────────────────────────
        CropType("Apple",         "red",    1, BigDecimal("5"),            1.5f),
        CropType("Tomato",        "red",    2, BigDecimal("20"),           2.25f),
        CropType("Strawberry",    "red",    3, BigDecimal("75"),           3.0f),
        CropType("Cherry",        "red",    4, BigDecimal("300"),          4.5f),
        CropType("Chili Pepper",  "red",    5, BigDecimal("1250"),         6.75f),
        // ── ORANGE ───────────────────────────────────────────────────────────
        CropType("Carrot",        "orange", 1, BigDecimal("25"),           4.5f),
        CropType("Orange",        "orange", 2, BigDecimal("100"),          6.75f),
        CropType("Mango",         "orange", 3, BigDecimal("375"),          9.0f),
        CropType("Pumpkin",       "orange", 4, BigDecimal("1500"),         13.5f),
        CropType("Persimmon",     "orange", 5, BigDecimal("6250"),         20.25f),
        // ── YELLOW ───────────────────────────────────────────────────────────
        CropType("Corn",          "yellow", 1, BigDecimal("125"),          13.5f),
        CropType("Banana",        "yellow", 2, BigDecimal("500"),          20.25f),
        CropType("Lemon",         "yellow", 3, BigDecimal("1875"),         27.0f),
        CropType("Squash",        "yellow", 4, BigDecimal("7500"),         40.5f),
        CropType("Pineapple",     "yellow", 5, BigDecimal("31250"),        60.75f),
        // ── GREEN ────────────────────────────────────────────────────────────
        CropType("Lettuce",       "green",  1, BigDecimal("625"),          40.5f),
        CropType("Cucumber",      "green",  2, BigDecimal("2500"),         60.75f),
        CropType("Broccoli",      "green",  3, BigDecimal("9375"),         81.0f),
        CropType("Pea",           "green",  4, BigDecimal("37500"),        121.5f),
        CropType("Kiwi",          "green",  5, BigDecimal("156250"),       182.25f),
        // ── BLUE ─────────────────────────────────────────────────────────────
        CropType("Blueberry",     "blue",   1, BigDecimal("3125"),         121.5f),
        CropType("Blue Corn",     "blue",   2, BigDecimal("12500"),        182.25f),
        CropType("Blue Pea",      "blue",   3, BigDecimal("46875"),        243.0f),
        CropType("Indigo Berry",  "blue",   4, BigDecimal("187500"),       364.5f),
        CropType("Elderberry",    "blue",   5, BigDecimal("781250"),       546.75f),
        // ── PURPLE ───────────────────────────────────────────────────────────
        CropType("Grape",         "purple", 1, BigDecimal("15625"),        364.5f),
        CropType("Eggplant",      "purple", 2, BigDecimal("62500"),        546.75f),
        CropType("Plum",          "purple", 3, BigDecimal("234375"),       729.0f),
        CropType("Purple Cabbage","purple", 4, BigDecimal("937500"),       1093.5f),
        CropType("Acai",          "purple", 5, BigDecimal("3906250"),      1640.25f),
        // ── PINK ─────────────────────────────────────────────────────────────
        CropType("Peach",         "pink",   1, BigDecimal("78125"),        1093.5f),
        CropType("Raspberry",     "pink",   2, BigDecimal("312500"),       1640.25f),
        CropType("Pomegranate",   "pink",   3, BigDecimal("1171875"),      2187.0f),
        CropType("Grapefruit",    "pink",   4, BigDecimal("4687500"),      3280.5f),
        CropType("Dragon Fruit",  "pink",   5, BigDecimal("19531250"),     4920.75f),
        // ── BROWN ────────────────────────────────────────────────────────────
        CropType("Wheat",         "brown",  1, BigDecimal("390625"),       3280.5f),
        CropType("Potato",        "brown",  2, BigDecimal("1562500"),      4920.75f),
        CropType("Coffee Bean",   "brown",  3, BigDecimal("5859375"),      6561.0f),
        CropType("Mushroom",      "brown",  4, BigDecimal("23437500"),     9841.5f),
        CropType("Truffle",       "brown",  5, BigDecimal("97656250"),     14762.25f),
        // ── WHITE ────────────────────────────────────────────────────────────
        CropType("Onion",         "white",  1, BigDecimal("1953125"),      9841.5f),
        CropType("Cauliflower",   "white",  2, BigDecimal("7812500"),      14762.25f),
        CropType("Garlic",        "white",  3, BigDecimal("29296875"),     19683.0f),
        CropType("Turnip",        "white",  4, BigDecimal("117187500"),    29524.5f),
        CropType("White Asparagus","white", 5, BigDecimal("488281250"),    44286.75f),
        // ── BLACK ────────────────────────────────────────────────────────────
        CropType("Blackberry",    "black",  1, BigDecimal("9765625"),      29524.5f),
        CropType("Black Bean",    "black",  2, BigDecimal("39062500"),     44286.75f),
        CropType("Black Olive",   "black",  3, BigDecimal("146484375"),    59049.0f),
        CropType("Black Sesame",  "black",  4, BigDecimal("585937500"),    88573.5f),
        CropType("Black Truffle", "black",  5, BigDecimal("2441406250"),   132860.25f),
    )

    fun forColor(color: String): List<CropType> = all.filter { it.color == color }.sortedBy { it.tier }
    fun tier1(color: String): CropType = forColor(color).first { it.tier == 1 }
    fun byId(id: String): CropType? = all.find { it.id == id }
}
