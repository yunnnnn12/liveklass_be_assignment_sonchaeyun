## 프로젝트 개요
- 온라인 강의 수강 신청 시스템
- 핵심 기능
  - 강의 개설/관리/조회
  - 수강 신청/취소/목록 조회
  - 수강 신청 정원 관리(동시성 환경)
  - 수강 취소 가능 기간 제한
  - 대기열(waitlist) 기능
  - 신청 내역 페이지네이션


## 기술 스택
- Language: Java
- Framework: Spring Boot
- ORM: JPA (Hibernate)
- DB: H2
- Test: JUnit5, AssertJ
- 기타: Spring Data JPA, Lombok

## 실행 방법
1. 프로젝트 클론 : git clone https://github.com/yunnnnn12/liveklass_be_assignment_sonchaeyun.git
2. 빌드 : ./gradlew clean build
3. 실행 : ./gradlew bootRun 또는 IDE(IntelliJ 등)에서 Application 실행
4. H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)
5. 접속 : http://localhost:8080


## API 목록 및 예시
### 1. 강의 관련
- POST /api/courses: 강의 등록
- GET /api/courses: 강의 목록 조회 (status 파라미터로 필터 가능)
- GET /api/courses/{courseId}: 강의 상세 조회 (courseId로 필터 가능)
- PATCH /api/courses/{id}/open: 강의 상태 OPEN 변경
- PATCH /api/courses/{id}/close: 강의 상태 CLOSE 변경

### 2. 수강 신청 관련
- POST /api/enrollments/enroll: 수강 신청 (Body: courseId, userName)
- GET /api/enrollments/me?userId={id}&page={page}&size={size}: 내 수강 신청 목록 조회 (Pageable 지원)
- GET /api/enrollments/course/{courseId}/students?page={page}&size={size}: 특정 강의 수강생 목록 조회 (크리에이터용, 페이징) 
- PATCH /api/enrollments/{enrollmentId}/confirm: 수강 신청 승인
- PATCH /api/enrollments/{enrollmentId}/cancel: 수강 신청 취소 (7일 제한 로직 포함)


## 데이터 모델 설명
1. Course: 강의 정보를 담으며 currentCount로 실시간 인원 관리
2. Enrollment: 수강 신청 내역을 관리하며 신청 상태와 확정 일시 저장
3. Classmate: 수강생 정보를 관리하며 중복 가입 방지
4. Waitlist: 정원 초과 시 신청한 사용자를 순차적으로 기록



## 요구사항 해석 및 가정 + 설계 결정과 이유
1. 강의 개설
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

2. 수강 신청
- Enrollment 엔티티를 별도로 구현
  - 해석 및 가정 : Course와 Classmate간 맵핑 관계를 포함하여 구현
  - 이유 : 강의, 수강생, 수강신청 정보 관리 용이 위해
- Waitlist 엔티티 구현
  - 이유 : 수강신청 도중 정원 초과 시 대기열에 저장하기 위해 Waitlist 엔티티 구현
- 목록 조회 시 Page 처리
  - 이유 : 대량 데이터 조회 시 한눈에 보기 쉽도록 구현

3. 동시성 처리
- 강의 조회 시 락(lock) 처리
  - 해석 및 가정 : 수강 신청과 결제 확정 처리 시 강의를 조회할 때 락 처리
  - 이유 : 동시에 여러 사용자가 신청할 경우 발생할 수 있는 데이터 불일치 예방
- Course 엔티티 validateAvailable 메서드 구현
  - 이유 : 정확한 상태 관리를 위해 엔티티에 예외 로직 구현

## 테스트 실행 방법
- ./gradlew test

## 미구현/제약 사항
1. 인증/인가 최소화
- userId를 헤더 또는 파라미터로 전달하여 인증/인가를 간단히 처리
- JWT 등 실제 인증 로직은 구현하지 않음

2. 실제 결제 연동 미구현
- 결제 확정은 단순 confirm 메서드 호출로 처리

3. 프론트엔드 미연동
- API 테스트는 Postman 등으로 확인 가능, 실제 UI는 구현하지 않음

## AI 활용 범위
- 과제 기능 사항을 처음 분석할 때 AI(ChatGPT 등)를 활용하여 명사, 동사, DTO 등 설계 요소를 어떻게 나눌지 기준을 참고
- AI가 제안한 로직과 설계 아이디어를 바탕으로 본인이 직접 코드 비교, 수정, 검증
- Spring Boot와 JPA 기반 코드 구조, 테스트 코드 작성 방법 등 예시를 확인


