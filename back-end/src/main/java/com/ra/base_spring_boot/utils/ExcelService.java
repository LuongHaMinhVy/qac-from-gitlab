package com.ra.base_spring_boot.utils;

import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.model.constants.Gender;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.AccountRoleRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final AccountRoleRepo accountRoleRepo;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportMembersToExcel(List<User> users) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Members");

        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "Account ID", "Username", "Email", "Full Name",
            "Phone", "Gender", "Date of Birth", "Address",
            "Is Active", "Email Verified", "Roles", "Created At", "Updated At"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            Account account = user.getAccount();

            List<RoleName> roles = accountRoleRepo.findRoleCodesByAccountId(account.getAccountId());
            String rolesStr = roles.stream()
                    .map(RoleName::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            row.createCell(0).setCellValue(user.getId() != null ? user.getId() : 0);
            row.createCell(1).setCellValue(account.getAccountId() != null ? account.getAccountId() : 0);
            row.createCell(2).setCellValue(account.getUsername() != null ? account.getUsername() : "");
            row.createCell(3).setCellValue(account.getEmail() != null ? account.getEmail() : "");
            row.createCell(4).setCellValue(user.getFullName() != null ? user.getFullName() : "");
            row.createCell(5).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(6).setCellValue(user.getGender() != null ? user.getGender().name() : "");
            row.createCell(7).setCellValue(user.getDateOfBirth() != null ? user.getDateOfBirth().format(DATE_FORMATTER) : "");
            row.createCell(8).setCellValue(user.getAddress() != null ? user.getAddress() : "");
            row.createCell(9).setCellValue(account.getIsActive() != null && account.getIsActive() ? "Yes" : "No");
            row.createCell(10).setCellValue(account.getEmailVerified() != null && account.getEmailVerified() ? "Yes" : "No");
            row.createCell(11).setCellValue(rolesStr);
            row.createCell(12).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(DATETIME_FORMATTER) : "");
            row.createCell(13).setCellValue(user.getUpdatedAt() != null ? user.getUpdatedAt().format(DATETIME_FORMATTER) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public List<MemberImportData> importMembersFromExcel(MultipartFile file) throws IOException {
        List<MemberImportData> members = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (isRowEmpty(row)) {
                continue;
            }

            MemberImportData member = new MemberImportData();
            member.setUsername(getCellValueAsString(row, 0));
            member.setEmail(getCellValueAsString(row, 1));
            member.setFullName(getCellValueAsString(row, 2));
            member.setPhone(getCellValueAsString(row, 3));

            String genderStr = getCellValueAsString(row, 4);
            if (genderStr != null && !genderStr.isEmpty()) {
                try {
                    member.setGender(Gender.valueOf(genderStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    member.setGender(null);
                }
            }

            String dobStr = getCellValueAsString(row, 5);
            if (dobStr != null && !dobStr.isEmpty()) {
                try {
                    member.setDateOfBirth(LocalDate.parse(dobStr, DATE_FORMATTER));
                } catch (Exception e) {
                    member.setDateOfBirth(null);
                }
            }

            member.setAddress(getCellValueAsString(row, 6));

            members.add(member);
        }

        workbook.close();
        return members;
    }

    private String getCellValueAsString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public static class MemberImportData {
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String address;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Gender getGender() {
            return gender;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}

