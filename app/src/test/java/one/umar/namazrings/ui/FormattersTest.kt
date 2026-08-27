package one.umar.namazrings.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormattersTest {
    @Test
    fun `Hijri date includes the Islamic month and year`() {
        assertEquals(
            "15 Rabi I 1448 AH",
            formatHijriDate(LocalDate.of(2026, 8, 28)),
        )
    }

    @Test
    fun `compact Hijri date omits the year`() {
        assertEquals(
            "15 Rabi I",
            formatHijriDate(LocalDate.of(2026, 8, 28), includeYear = false),
        )
    }
}
