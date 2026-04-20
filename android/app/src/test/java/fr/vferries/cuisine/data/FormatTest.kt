package fr.vferries.cuisine.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatTest {

    @Test fun formatUnit_empty_when_unit_null_or_empty() {
        assertEquals("", formatUnit("1", null))
        assertEquals("", formatUnit("1", ""))
    }

    @Test fun formatUnit_applies_display_map_for_cac_cas() {
        assertEquals("c. à c.", formatUnit("1", "càc"))
        assertEquals("c. à s.", formatUnit("6", "càs"))
    }

    @Test fun formatUnit_pluralizes_brin_sachet_bouquet_gousse_pincee_when_qty_over_1() {
        assertEquals("brins", formatUnit("2", "brin"))
        assertEquals("sachets", formatUnit("3", "sachet"))
        assertEquals("bouquets", formatUnit("2", "bouquet"))
        assertEquals("gousses", formatUnit("4", "gousse"))
        assertEquals("pincées", formatUnit("2", "pincée"))
    }

    @Test fun formatUnit_keeps_singular_when_qty_le_1() {
        assertEquals("brin", formatUnit("1", "brin"))
        assertEquals("brin", formatUnit("0.5", "brin"))
    }

    @Test fun formatUnit_does_not_pluralize_metric_units() {
        assertEquals("g", formatUnit("200", "g"))
        assertEquals("kg", formatUnit("2", "kg"))
        assertEquals("ml", formatUnit("500", "ml"))
        assertEquals("c. à c.", formatUnit("6", "càc"))
    }

    @Test fun formatQty_returns_null_when_qty_empty() {
        assertNull(formatQty("", "g"))
    }

    @Test fun formatQty_returns_qty_only_when_no_unit() {
        assertEquals("3", formatQty("3", null))
        assertEquals("3", formatQty("3", ""))
    }

    @Test fun formatQty_combines_qty_and_formatted_unit() {
        assertEquals("2 brins", formatQty("2", "brin"))
        assertEquals("1 brin", formatQty("1", "brin"))
        assertEquals("6 c. à c.", formatQty("6", "càc"))
    }

    @Test fun scaleQuantityText_preserves_integer_results() {
        assertEquals("9", scaleQuantityText("6", 1.5))
        assertEquals("1", scaleQuantityText("2", 0.5))
    }

    @Test fun scaleQuantityText_keeps_decimals_when_needed() {
        assertEquals("1.5", scaleQuantityText("1", 1.5))
    }

    @Test fun scaleQuantityText_leaves_non_numeric_untouched() {
        assertEquals("quelques", scaleQuantityText("quelques", 2.0))
        assertEquals("", scaleQuantityText("", 2.0))
    }

    @Test fun pluralizeName_keeps_name_when_qty_le_1() {
        assertEquals("poêle", pluralizeName(1, "poêle"))
    }

    @Test fun pluralizeName_adds_s_on_simple_names() {
        assertEquals("poêles", pluralizeName(2, "poêle"))
        assertEquals("bols", pluralizeName(3, "bol"))
    }

    @Test fun pluralizeName_keeps_invariant_when_ends_in_s_x_z() {
        assertEquals("baguettes", pluralizeName(2, "baguettes"))
        assertEquals("bois", pluralizeName(2, "bois"))
    }

    @Test fun pluralizeName_pluralizes_only_first_word_of_compound() {
        assertEquals("planches à découper", pluralizeName(2, "planche à découper"))
        assertEquals("cuillères en bois", pluralizeName(2, "cuillère en bois"))
    }
}
