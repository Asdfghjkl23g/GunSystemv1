# วิธีการ Compile Plugin

## ต้องติดตั้ง Maven ก่อน
ดาวน์โหลด Maven จาก: https://maven.apache.org/download.cgi

## วิธี Compile

### Windows
```cmd
cd GunSystem
mvn clean package
```

### Linux/Mac
```bash
cd GunSystem
mvn clean package
```

## ไฟล์ที่ได้
หลังจาก compile เสร็จ ไฟล์ plugin จะอยู่ที่:
```
GunSystem/target/GunSystem-1.0.0.jar
```

## การติดตั้ง
1. คัดลอกไฟล์ `GunSystem-1.0.0.jar`
2. วางในโฟลเดอร์ `plugins` ของเซิร์ฟเวอร์
3. รีสตาร์ทเซิร์ฟเวอร์

## หากไม่มี Maven
คุณสามารถ compile ผ่าน IDE ได้:
- IntelliJ IDEA: Import เป็น Maven Project แล้วกด Build
- Eclipse: Import เป็น Maven Project แล้วกด Export > JAR file

## ทางเลือกอื่น
หากไม่ต้องการ compile เอง สามารถใช้ Online Maven Compiler:
1. https://www.compilejava.net/ (สำหรับโปรเจคเล็ก)
2. GitHub Actions (สำหรับโปรเจคใหญ่)
3. ใช้ IDE online เช่น Replit, CodeSandbox
