# ADR-000: ADR 작성 규칙

| 항목 | 내용 |
|------|------|
| 상태 | 승인됨 (Accepted) |
| 날짜 | 2026-04-09 |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)

프로젝트 초기에 아키텍처 결정 사항들이 `CHANGELOG.md`에 서술형으로 기록되어 있다. 의사결정의 배경과 근거를 체계적으로 보존하고, 미래의 결정 시 참고하기 위한 구조화된 형식이 필요하다.

## 결정 (Decision)

Michael Nygard의 ADR(Architecture Decision Record) 형식을 기반으로 한국어로 작성한다.

**파일 위치 및 명명:**
- 위치: `docs/adr/ADR-NNN-제목.md`
- 번호: 3자리 zero-padding (`ADR-000`, `ADR-001`, ...)
- 제목: 결정의 핵심을 담은 한국어 슬러그 (하이픈 구분)

**상태값:**
- `제안됨 (Proposed)` — 검토 중
- `승인됨 (Accepted)` — 적용 중
- `폐기됨 (Deprecated)` — 더 이상 유효하지 않음
- `대체됨 (Superseded by ADR-NNN)` — 다른 ADR로 교체됨

**ADR 본문 형식:**

```markdown
# ADR-NNN: [제목]

| 항목 | 내용 |
|------|------|
| 상태 | [상태값] |
| 날짜 | YYYY-MM-DD |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)
결정이 필요했던 배경, 문제 상황, 제약 조건

## 결정 (Decision)
선택한 방향과 이유

## 대안 (Alternatives Considered)
검토했으나 채택하지 않은 대안과 그 이유

## 결과 (Consequences)
### 긍정적 결과
### 부정적 결과 / 트레이드오프
### 후속 과제
```

**CHANGELOG.md와의 역할 분리:**
- `CHANGELOG.md` — 변경 이력(What changed)
- `docs/adr/` — 의사결정 근거(Why we decided)

## 대안 (Alternatives Considered)

- **CHANGELOG.md 계속 사용**: 서술형 기록으로 추적과 참조가 어렵고, 결정 간 관계 파악이 불편하다.
- **위키 문서**: 별도 도구 의존, 코드와 함께 버전 관리가 되지 않는다.

## 결과 (Consequences)

### 긍정적 결과
- 의사결정 근거를 코드베이스와 함께 버전 관리할 수 있다.
- 미래의 유사한 결정 시 과거 ADR을 참고할 수 있다.
- PR/코드 리뷰 시 설계 의도를 명확히 전달할 수 있다.

### 부정적 결과 / 트레이드오프
- 모든 주요 결정마다 ADR 작성 비용이 발생한다.
- 소급 작성된 ADR은 원래의 결정 시점과 날짜가 다를 수 있다.

### 후속 과제
- `CHANGELOG.md` 상단에 "상세 결정 근거는 `docs/adr/` 참조" 안내 추가
