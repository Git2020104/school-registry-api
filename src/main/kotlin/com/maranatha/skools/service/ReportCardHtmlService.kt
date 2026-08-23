package com.maranatha.skools.service

import com.maranatha.skools.models.StudentReportCardResponse

object ReportCardHtmlService {

    fun generateReportCardHtml(report: StudentReportCardResponse): String {
        val subjectRowsHtml = report.subjects.joinToString("") { sub ->
            val paperBreakdown = sub.paperScores.joinToString(", ") { "${it.paperName}: ${it.score}/${it.maxMarks}" }
            """
            <tr>
                <td style="padding: 6px; border: 1px solid #ddd; font-weight: bold;">${sub.subjectCode} - ${sub.subjectName}</td>
                <td style="padding: 6px; border: 1px solid #ddd; font-size: 11px;">$paperBreakdown</td>
                <td style="padding: 6px; border: 1px solid #ddd; text-align: center; font-weight: bold;">${sub.averageScore}%</td>
                <td style="padding: 6px; border: 1px solid #ddd; text-align: center; font-weight: bold;">${sub.grade}</td>
                <td style="padding: 6px; border: 1px solid #ddd; text-align: center;">${sub.aggregateValue}</td>
                <td style="padding: 6px; border: 1px solid #ddd; font-size: 11px;">${sub.teacherComment}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8"/>
            <style>
                @page { size: A4; margin: 15mm; }
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #222; font-size: 12px; line-height: 1.4; }
                .header { text-align: center; border-bottom: 2px solid #1a365d; padding-bottom: 10px; margin-bottom: 15px; }
                .school-title { font-size: 20px; font-weight: bold; color: #1a365d; text-transform: uppercase; margin: 0; }
                .sub-title { font-size: 12px; color: #555; margin: 2px 0 0 0; }
                .report-title { font-size: 14px; font-weight: bold; margin-top: 8px; text-decoration: underline; letter-spacing: 1px; }
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                .meta-table td { padding: 4px 0; font-size: 11px; }
                .marks-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                .marks-table th { background-color: #1a365d; color: #ffffff; padding: 6px; font-size: 11px; border: 1px solid #1a365d; text-align: left; }
                .summary-container { width: 100%; margin-bottom: 15px; }
                .summary-table { width: 100%; border-collapse: collapse; border: 1px solid #1a365d; }
                .summary-table td { padding: 8px; font-weight: bold; border: 1px solid #ddd; font-size: 12px; }
                .comment-box { border: 1px solid #ccc; padding: 8px; margin-bottom: 10px; border-radius: 4px; background-color: #f9f9f9; }
                .comment-title { font-weight: bold; color: #1a365d; margin-bottom: 4px; font-size: 11px; }
                .footer { margin-top: 30px; width: 100%; }
                .signature-line { border-top: 1px solid #000; width: 180px; text-align: center; font-size: 10px; padding-top: 4px; }
            </style>
        </head>
        <body>

            <div class="header">
                <h1 class="school-title">Maranatha High School</h1>
                <p class="sub-title">P.O. Box 1024, Kampala, Uganda | Tel: +256 700 000 000</p>
                <div class="report-title">STUDENT ACADEMIC REPORT CARD - ${report.examTermName.uppercase()}</div>
            </div>

            <table class="meta-table">
                <tr>
                    <td><strong>Student Name:</strong> ${report.fullName}</td>
                    <td><strong>Admission No:</strong> ${report.admissionNumber}</td>
                </tr>
                <tr>
                    <td><strong>Class &amp; Stream:</strong> ${report.className} ${report.streamName}</td>
                    <td><strong>Position in Stream:</strong> ${report.positionInStream} of ${report.totalStudentsInStream}</td>
                </tr>
                <tr>
                    <td><strong>Academic Year:</strong> ${report.year}</td>
                    <td><strong>Term:</strong> Term ${report.term}</td>
                </tr>
            </table>

            <table class="marks-table">
                <thead>
                    <tr>
                        <th style="width: 25%;">Subject</th>
                        <th style="width: 25%;">Paper Breakdown</th>
                        <th style="width: 10%; text-align: center;">Avg Score</th>
                        <th style="width: 10%; text-align: center;">Grade</th>
                        <th style="width: 8%; text-align: center;">Agg</th>
                        <th style="width: 22%;">Remarks</th>
                    </tr>
                </thead>
                <tbody>
                    $subjectRowsHtml
                </tbody>
            </table>

            <div class="summary-container">
                <table class="summary-table">
                    <tr style="background-color: #f0f4f8;">
                        <td>Total Marks: ${report.totalMarks}</td>
                        <td>Overall Average: ${report.overallAverage}%</td>
                        <td>Best 8 Agg: ${report.best8Aggregate ?: "N/A"}</td>
                        <td>Division Result: <span style="color: #1a365d;">${report.division}</span></td>
                    </tr>
                </table>
            </div>

            <div class="comment-box">
                <div class="comment-title">CLASS TEACHER'S REMARKS:</div>
                <div>"${report.classTeacherComment}"</div>
            </div>

            <div class="comment-box">
                <div class="comment-title">HEADTEACHER'S REMARKS:</div>
                <div>"${report.headteacherComment}"</div>
            </div>

            <table class="footer">
                <tr>
                    <td style="width: 50%;">
                        <div class="signature-line">Class Teacher Signature</div>
                    </td>
                    <td style="width: 50%; text-align: right;">
                        <div class="signature-line" style="margin-left: auto;">Headteacher Signature &amp; Stamp</div>
                    </td>
                </tr>
            </table>

        </body>
        </html>
        """.trimIndent()
    }
}