package com.example

import com.example.data.model.CourseCatalog
import com.example.ui.viewmodel.FeeViewModel
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCourseCatalogSpecifications() {
    val courses = CourseCatalog.courses
    assertEquals(6, courses.size)

    val bca = courses.first { it.name == "BCA" }
    assertEquals(6, bca.totalSemesters)

    val bed = courses.first { it.name == "B.Ed" }
    assertEquals(4, bed.totalSemesters)

    val mscZoo = courses.first { it.name == "M.Sc Zoology" }
    assertEquals(4, mscZoo.totalSemesters)

    val mscChem = courses.first { it.name == "M.Sc Chemistry" }
    assertEquals(4, mscChem.totalSemesters)

    val mscPhy = courses.first { it.name == "M.Sc Physics" }
    assertEquals(4, mscPhy.totalSemesters)

    val msc = courses.first { it.name == "M.Sc" }
    assertEquals(4, msc.totalSemesters)
  }

  @Test
  fun testIndianRupeeCurrencyFormatting() {
    val formatted = FeeViewModel.formatCurrency(25000.0)
    assertTrue("Should contain rupee symbol: $formatted", formatted.contains("₹"))
  }
}
