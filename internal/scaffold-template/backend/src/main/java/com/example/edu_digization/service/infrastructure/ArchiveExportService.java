package com.example.edu_digization.service.infrastructure;

import com.example.edu_digization.common.ArchiveType;
import com.example.edu_digization.vo.ArchiveListVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 归档清单 Excel 导出服务（Apache POI）
 */
@Component
public class ArchiveExportService {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String[] HEADERS = {
            "学院", "教研室", "学期", "课程号", "课程名称",
            "教师工号", "教师姓名", "档案类型", "状态", "版本", "提交时间"
    };

    /**
     * 将档案列表写入 .xlsx 并输出到响应
     */
    public void export(HttpServletResponse response, List<ArchiveListVO> data, String filename) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("归档清单");

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ArchiveListVO vo : data) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(safe(vo.getCollegeName()));
                row.createCell(1).setCellValue(safe(vo.getDeptName()));
                row.createCell(2).setCellValue(safe(vo.getSemesterName()));
                row.createCell(3).setCellValue(safe(vo.getCourseCode()));
                row.createCell(4).setCellValue(safe(vo.getCourseName()));
                row.createCell(5).setCellValue(safe(vo.getTeacherNo()));
                row.createCell(6).setCellValue(safe(vo.getTeacherName()));
                row.createCell(7).setCellValue(
                        ArchiveType.PRE.equals(vo.getArchiveType()) ? "课前" : "课后");
                row.createCell(8).setCellValue(toStatusCn(vo.getStatus()));
                row.createCell(9).setCellValue(
                        vo.getVersionNo() == null ? "" : "v" + vo.getVersionNo());
                row.createCell(10).setCellValue(
                        vo.getSubmitTime() == null ? "" : vo.getSubmitTime().format(DT_FORMATTER));
            }

            // 自适应列宽
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=UTF-8''" + encodedName);
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new IllegalStateException("导出 Excel 失败：" + e.getMessage(), e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String toStatusCn(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "NOT_SUBMITTED": return "未提交";
            case "PENDING": return "待初审";
            case "FIRST_REVIEW": return "待中间审核";
            case "MIDDLE_REVIEW": return "待终审";
            case "SECOND_REVIEW": return "终审中";
            case "ARCHIVED": return "已归档";
            case "REJECTED": return "已驳回";
            default: return status;
        }
    }
}
