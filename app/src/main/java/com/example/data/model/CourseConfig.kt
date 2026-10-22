package com.example.data.model

data class CourseInfo(
    val name: String,
    val code: String,
    val description: String,
    val totalSemesters: Int,
    val standardFeePerSemester: Double
) {
    val semesterOptions: List<String>
        get() = (1..totalSemesters).map { "Semester $it" }
}

object CourseCatalog {
    val courses = listOf(
        CourseInfo(
            name = "BCA",
            code = "BCA",
            description = "Bachelor of Computer Applications (6 Semesters)",
            totalSemesters = 6,
            standardFeePerSemester = 25000.0
        ),
        CourseInfo(
            name = "B.Ed",
            code = "B.ED",
            description = "Bachelor of Education (4 Semesters)",
            totalSemesters = 4,
            standardFeePerSemester = 32000.0
        ),
        CourseInfo(
            name = "M.Sc Zoology",
            code = "MSC-ZOO",
            description = "Master of Science in Zoology (4 Semesters)",
            totalSemesters = 4,
            standardFeePerSemester = 28000.0
        ),
        CourseInfo(
            name = "M.Sc Chemistry",
            code = "MSC-CHEM",
            description = "Master of Science in Chemistry (4 Semesters)",
            totalSemesters = 4,
            standardFeePerSemester = 30000.0
        ),
        CourseInfo(
            name = "M.Sc Physics",
            code = "MSC-PHY",
            description = "Master of Science in Physics (4 Semesters)",
            totalSemesters = 4,
            standardFeePerSemester = 30000.0
        ),
        CourseInfo(
            name = "M.Sc",
            code = "MSC",
            description = "Master of Science (4 Semesters)",
            totalSemesters = 4,
            standardFeePerSemester = 26000.0
        )
    )

    fun getAllCourseGradeStrings(): List<String> {
        val list = mutableListOf<String>()
        courses.forEach { course ->
            for (sem in 1..course.totalSemesters) {
                list.add("${course.name} - Sem $sem")
            }
        }
        return list
    }
}
