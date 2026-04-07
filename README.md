# 📌 온라인 수강 신청 시스템
## 1. 프로젝트 개요
- 온라인 강의 수강 신청 시스템
- 핵심 기능
  - 강의 개설/관리/조회
  - 수강 신청/취소/목록 조회
  - 수강 신청 정원 관리(동시성 환경)
  - 수강 취소 가능 기간 제한
  - 대기열(waitlist) 기능
  - 신청 내역 페이지네이션


## 2. 기술 스택
### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.4
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: H2 (In-memory)
- **API Docs**: Swagger 

### Test
- **Framework**: JUnit5
- **Library**: AssertJ 

### Tools & Etc
- **Build Tool**: Gradle
- **Library**: Lombok

## 3. 실행 방법
1. **프로젝트 클론 및 이동** : 
- 1) git clone https://github.com/yunnnnn12/liveklass_be_assignment_sonchaeyun.git
- 2) cd liveklass_be_assignment_sonchaeyun
2. **빌드** : ./gradlew clean build
3. **실행** : ./gradlew bootRun 또는 IDE(IntelliJ 등)에서 Application 실행
4. **H2 Console** : http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)
5. **접속** : http://localhost:8080


## 4. API 목록 및 예시
### 1. 강의 관련
- **POST /api/courses**: 강의 등록
- **GET /api/courses**: 강의 목록 조회 (status 파라미터로 필터 가능)
- **GET /api/courses/{courseId}**: 강의 상세 조회 (courseId로 필터 가능)
- **PATCH /api/courses/{id}/open**: 강의 상태 OPEN 변경
- **PATCH /api/courses/{id}/close**: 강의 상태 CLOSE 변경

### 2. 수강 신청 관련
- **POST /api/enrollments/enroll**: 수강 신청 (Body: courseId, userName)
- **GET /api/enrollments/me?userId={id}&page={page}&size={size}**: 내 수강 신청 목록 조회 (Pageable 지원)
- **GET /api/enrollments/course/{courseId}/students?page={page}&size={size}**: 특정 강의 수강생 목록 조회 (크리에이터용, 페이징) 
- **PATCH /api/enrollments/{enrollmentId}/confirm**: 수강 신청 승인
- **PATCH /api/enrollments/{enrollmentId}/cancel**: 수강 신청 취소 (7일 제한 로직 포함)

## 5. API 명세
Swagger UI에서 전체 API 명세 및 요청/응답 예시 확인 가능:
[Swagger UI](http://localhost:8080/swagger-ui/index.html)


## 6. 데이터 모델 설명
1. **Course**: 강의 정보를 담으며 currentCount로 실시간 인원 관리
2. **Enrollment**: 수강 신청 내역을 관리하며 신청 상태와 확정 일시 저장
3. **Classmate**: 수강생 정보를 관리하며 중복 가입 방지
4. **Waitlist**: 정원 초과 시 신청한 사용자를 순차적으로 기록


## 7. DB 스키마 / ERD 설명
### 1. Classmate (수강생)
- **id** (PK, Long, auto-generated) : 수강생 고유 ID
- **name** (String, unique, not null) : 수강생 이름
- **관계**
  - 1:N Enrollment : 한 수강생은 여러 수강 신청 가능

### 2. Course (강의)
- **id** (PK, Long, auto-generated) : 강의 고유 ID
- **title** (String) : 강의 제목
- **description** (String) : 강의 설명
- **price** (int) : 강의 가격
- **maxCapacity** (int) : 최대 정원
- **currentCount** (int) : 현재 수강생 수
- **startDate / endDate** (LocalDateTime) : 강의 시작/종료일
- **classStatus** (Enum: DRAFT / OPEN / CLOSED) : 강의 상태
- **관계**
  - 1:N Enrollment : 한 강의에 여러 수강 신청 가능
  - 1:N Waitlist : 정원 초과 시 대기자 목록 관리

### 3. Enrollment (수강신청)
- **id** (PK, Long, auto-generated) : 수강신청 고유 ID
- **classmate_id** (FK → Classmate.id) : 수강생 참조
- **course_id** (FK → Course.id) : 강의 참조
- **enrollmentStatus** (Enum: PENDING / CONFIRMED / CANCELED) : 수강신청 상태
- **enrolledDate** (LocalDateTime) : 신청 일시
- **confirmedAt** (LocalDateTime) : 결제 확정 일시
- **관계**
  - N:1 Classmate
  - N:1 Course

### 4. Waitlist (대기열)
- **id** (PK, Long, auto-generated) : 대기열 고유 ID
- **course_id** (FK → Course.id) : 강의 참조
- **userName** (String) : 신청자 이름
- **createdAt** (LocalDateTime) : 대기 등록 일시
- **관계**
  - N:1 Course

### 5. 관계 요약
- **Classmate 1:N Enrollment** : 한 수강생은 여러 강의를 신청할 수 있음
- **Course 1:N Enrollment** : 한 강의에 여러 수강 신청 가능
- **Course 1:N Waitlist** : 정원 초과 시 대기자 관리


## 8. 요구사항 해석 및 가정 + 설계 결정과 이유
### 1. 강의 개설
- Course 엔티티와 DTO 기반 설계
  - 해석 및 가정 : 강사가 강의 제목, 설명 등 정보를 DTO로 요청하면 강의 엔티티 생성 후 DB에 저장
  - 이유 : 데이터 전달 구조 명확히 하여 유지보수 향상
- 강의 상태를 enum으로 관리
  - 이유 : 상태 값의 일관성과 상태별 로직 처리 간편화를 위해
- 강사 재량으로 OPEN/CLOSED 관리
  - 이유 : 수강 기간과 강사 계획을 유연하게 반영하기 위해
- 강의 오픈 시 validate 구현
  - 해석 및 가정 : 강의 오픈 조건 미충족 시 Course 엔티티에서 예외 발생하도록 구현
  - 이유 : 컨트롤러와 서비스 로직 최소화와 정확한 상태 관리를 위해 엔티티에 예외 로직 구현

### 2. 수강 신청
- Enrollment 엔티티를 별도로 구현
  - 해석 및 가정 : Course와 Classmate간 맵핑 관계를 포함하여 구현
  - 이유 : 강의, 수강생, 수강신청 정보 관리 용이 위해
- Waitlist 엔티티 구현
  - 이유 : 수강신청 도중 정원 초과 시 대기열에 저장하기 위해 Waitlist 엔티티 구현
- 목록 조회 시 Page 처리
  - 이유 : 대량 데이터 조회 시 한눈에 보기 쉽도록 구현

### 3. 동시성 처리
- 강의 조회 시 락(lock) 처리
  - 해석 및 가정 : 수강 신청과 결제 확정 처리 시 강의를 조회할 때 락 처리
  - 이유 : 동시에 여러 사용자가 신청할 경우 발생할 수 있는 데이터 불일치 예방
- Course 엔티티 validateAvailable 메서드 구현
  - 이유 : 정확한 상태 관리를 위해 엔티티에 예외 로직 구현

## 9. 테스트 실행 방법
### 1. 테스트 실행
- 터미널(또는 CMD)에서 프로젝트 루트 폴더로 이동한 후 다음 명령어 입력
  - `./gradlew test`
### 2. 테스트 결과 
- 경로 : `build/reports/tests/test/index.html`

## 10. 미구현/제약 사항
### 1. 인증/인가 최소화
- userId를 헤더 또는 파라미터로 전달하여 인증/인가를 간단히 처리
- JWT 등 실제 인증 로직은 구현하지 않음

### 2. 실제 결제 연동 미구현
- 결제 확정은 단순 confirm 메서드 호출로 처리

### 3. 프론트엔드 미연동
- API 테스트는 Postman 등으로 확인 가능, 실제 UI는 구현하지 않음

## 11. AI 활용 범위
1. 과제 기능 사항을 처음 분석할 때 AI(ChatGPT, Gemini)를 활용하여 명사, 동사, DTO 등 설계 요소를 어떻게 나눌지 기준을 참고하여 구현
2. AI(ChatGPT, Gemini)를 참고하여 객체지향적 코드 작성(OOP 설계 원칙, 엔티티 역할 분리 등) 기준을 고민하며 구현
3. Spring Boot와 JPA 기반 코드 구조, 테스트 코드 작성 방법 등 예시를 참고하여 코드 구현
4. 동시성 환경에서의 정원 초과 문제를 해결하기 위해 AI를 이용하여 '비관적 락(Pessimistic Lock)'의 개념을 이해하고, 이를 실제 코드와 테스트 로그를 통해 직접 검증

## 12. 느낀 점
- 이번 과제를 수행하며 가장 고민했던 지점은 "어디에 어떤 로직을 두는 것이 가장 클린한 코드인가" 였다. AI와 다양한 자료를
참고하며 엔티티가 스스로 검증 로직을 갖게 함으로써, 서비스 로직은 간결해지고 객체는 더 명확한 책임을 갖게 된다는 점을 알게되었고,
클린코드 작성에 대한 학습을 이어가고 싶다는 생각이 들었다. 또한, 지금은 H2 DB로 구현하였지만 추후 MySQL을 도입해, 실제 환경과 비슷한 인프라를 구축해보고 싶다.



