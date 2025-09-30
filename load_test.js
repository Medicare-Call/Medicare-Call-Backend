
import http from 'k6/http';
import { check, sleep } from 'k6';


/*
* 1분 동안 50명의 가상 사용자(VU)로 램프업
* 다음 3분 동안 100명의 VU를 유지
* 1분 동안 VU를 0으로 램프다운
*
* HTTP 요청 실패율은 1% 미만
* 95%의 요청은 200ms 미만이 되도록
* */
export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '3m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<200'],
  },
};

const BASE_URL = 'https://medicare-call.shop/api';
const PRESET_REFRESH_TOKEN = ''
const ELDER_ID = '';

export function setup() {
  const refreshHeaders = {
    'Content-Type': 'application/json',
    'Refresh-Token': PRESET_REFRESH_TOKEN,
  };

  const res = http.post(`${BASE_URL}/auth/refresh`, null, { headers: refreshHeaders });
  check(res, { 'setup: Access Token 갱신 상태 200 확인': (r) => r.status === 200 });

  console.log(`Setup Response Status: ${res.status}`);
  console.log(`Setup Response Body: ${res.body}`);
  console.log(`Setup Response Headers: ${JSON.stringify(res.headers)}`);

  const authToken = res.json('accessToken');
  const newRefreshToken = res.json('refreshToken');

  if (!authToken || !newRefreshToken || PRESET_REFRESH_TOKEN === 'YOUR_ACTUAL_REFRESH_TOKEN_HERE') {
    console.error('setup: 유효한 Refresh Token이 없거나 Access Token 갱신에 실패했습니다.');
    throw new Error('setup: Failed to refresh token or preset refresh token is not valid');
  }

  return { authToken: authToken, refreshToken: newRefreshToken };
}

export default function (data) {
  const authToken = data.authToken;
  const authHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authToken}`,
  };
  let res;
  const today = new Date().toISOString().slice(0, 10);

  // 1. 홈 화면 조회 테스트
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/home`, { headers: authHeaders });
  check(res, { '홈 화면 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`1. /elders/${ELDER_ID}/home GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 2. 현재 회원 정보 가져오기
  res = http.get(`${BASE_URL}/member`, { headers: authHeaders });
  check(res, { '회원 정보 가져오기 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`2. /member GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 3. 어르신 목록 가져오기
  res = http.get(`${BASE_URL}/elders`, { headers: authHeaders });
  check(res, { '어르신 목록 가져오기 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`3. /elders GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 4. 어르신 건강정보 조회
  res = http.get(`${BASE_URL}/elders/health-info`, { headers: authHeaders });
  check(res, { '어르신 건강 정보 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`4. /elders/health-info GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 5. 어르신 건강정보 등록 및 수정
  // const elderHealthInfoPayload = JSON.stringify({
  //   diseaseList: ['고혈압', '당뇨'],
  //   medicationCycle: '매일 아침',
  //   specialNote: '특별한 이상 없음',
  // });
  // res = http.post(`${BASE_URL}/elders/${ELDER_ID}/health-info`, elderHealthInfoPayload, { headers: authHeaders });
  // check(res, { '어르신 건강 정보 등록 및 수정 상태 201 확인': (r) => r.status === 201 });
  // if (res.status !== 201) console.log(`5. /elders/{elderId}/health-info POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  // sleep(1);

  // 6. 주간 혈당 데이터 조회 (공복)
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/blood-sugar/weekly?counter=0&type=BEFORE_MEAL`, { headers: authHeaders });
  check(res, { '주간 혈당 (공복) 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`6. /elders/{elderId}/blood-sugar/weekly (BEFORE_MEAL) GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 7. 주간 혈당 데이터 조회 (식후)
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/blood-sugar/weekly?counter=0&type=AFTER_MEAL`, { headers: authHeaders });
  check(res, { '주간 혈당 (식후) 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`7. /elders/{elderId}/blood-sugar/weekly (AFTER_MEAL) GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 8. 어르신 전화 시간대 등록 및 수정
  // const careCallSettingPayload = JSON.stringify({
  //   firstCallTime: '09:00',
  //   secondCallTime: '13:00',
  //   thirdCallTime: '18:00',
  //   callRecurrenceType: 'DAILY',
  // });
  // res = http.post(`${BASE_URL}/elders/${ELDER_ID}/care-call-setting`, careCallSettingPayload, { headers: authHeaders });
  // check(res, { '케어콜 설정 상태 200 확인': (r) => r.status === 200 });
  // if (res.status !== 200) console.log(`8. /elders/{elderId}/care-call-setting POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  // sleep(1);

  // 9. 어르신 전화 시간대 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/care-call-setting`, { headers: authHeaders });
  check(res, { '케어콜 설정 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`9. /elders/{elderId}/care-call-setting GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 10. 어르신 등록
  // const registerElderPayload = JSON.stringify({
  //   name: `테스트어르신${__VU}`,
  //   birthDate: '1950-01-01',
  //   gender: 'MALE',
  //   phone: `010${Math.floor(10000000 + Math.random() * 90000000)}`,
  //   relationship: 'SON',
  //   residenceType: 'ALONE',
  // });
  // res = http.post(`${BASE_URL}/elders`, registerElderPayload, { headers: authHeaders });
  // check(res, { '어르신 등록 상태 200 확인': (r) => r.status === 200 });
  // const newElderId = res.json('id');
  // if (res.status !== 200) console.log(`10. /elders POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  // sleep(1);

  // 11. 어르신 개인정보 수정 (등록된 어르신 ID 사용)
  // if (newElderId) {
  //   const updateElderPayload = JSON.stringify({
  //     name: `수정된테스트어르신${__VU}`,
  //     birthDate: '1950-01-01',
  //     gender: 'FEMALE',
  //     phone: `010${Math.floor(10000000 + Math.random() * 90000000)}`,
  //     relationship: 'DAUGHTER',
  //     residenceType: 'WITH_FAMILY',
  //   });
  //   res = http.post(`${BASE_URL}/elders/${newElderId}`, updateElderPayload, { headers: authHeaders });
  //   check(res, { '어르신 정보 수정 상태 200 확인': (r) => r.status === 200 });
  //   if (res.status !== 200) console.log(`11. /elders/{newElderId} POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  //   sleep(1);
  // }

  // 12. 날짜별 건강 징후 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/health-analysis?date=${today}`, { headers: authHeaders });
  check(res, { '날짜별 건강 징후 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`12. /elders/{elderId}/health-analysis GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 13. 홈 화면 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/home`, { headers: authHeaders });
  check(res, { '홈 화면 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`13. /elders/{elderId}/home GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 14. 날짜별 식사 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/meals?date=${today}`, { headers: authHeaders });
  check(res, { '날짜별 식사 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`14. /elders/{elderId}/meals GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 15. 날짜별 복약 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/medication?date=${today}`, { headers: authHeaders });
  check(res, { '날짜별 복약 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`15. /elders/{elderId}/medication GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 16. 내 정보 수정
  // const updateMemberInfoPayload = JSON.stringify({
  //   name: `수정된회원${__VU}`,
  //   gender: 'FEMALE',
  //   birthDate: '1990-05-15',
  // });
  // res = http.post(`${BASE_URL}/member`, updateMemberInfoPayload, { headers: authHeaders });
  // check(res, { '내 정보 수정 상태 200 확인': (r) => r.status === 200 });
  // if (res.status !== 200) console.log(`16. /member POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  // sleep(1);

  // 17. 날짜별 심리 상태 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/mental-analysis?date=${today}`, { headers: authHeaders });
  check(res, { '날짜별 심리 상태 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`17. /elders/{elderId}/mental-analysis GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 18. 결제 요청 생성
  // const naverPayReservePayload = JSON.stringify({
  //   productName: '메디케어콜 스탠다드 플랜',
  //   productCount: 1,
  //   totalPayAmount: 19000,
  //   taxScopeAmount: 19000,
  //   taxExScopeAmount: 0,
  //   returnUrl: 'https://example.com/payment/complete',
  //   productItems: [
  //     {
  //       categoryType: 'ETC',
  //       categoryId: 'ETC',
  //       uid: 'PRODUCT_123',
  //       name: '메디케어콜 스탠다드 플랜',
  //       count: 1,
  //     },
  //   ],
  // });
  // res = http.post(`${BASE_URL}/payments/reserve`, naverPayReservePayload, { headers: authHeaders });
  // check(res, { '결제 요청 생성 상태 200 확인': (r) => r.status === 200 });
  // if (res.status !== 200) console.log(`18. /payments/reserve POST Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  // sleep(1);

  // 19. 공지사항 목록 조회
  res = http.get(`${BASE_URL}/notices`, { headers: authHeaders });
  check(res, { '공지사항 목록 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`19. /notices GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 20. 날짜별 수면 데이터 조회
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/sleep?date=${today}`, { headers: authHeaders });
  check(res, { '날짜별 수면 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`20. /elders/{elderId}/sleep GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 21. 회원의 어르신 구독 정보 조회
  res = http.get(`${BASE_URL}/elders/subscriptions`, { headers: authHeaders });
  check(res, { '회원의 어르신 구독 정보 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`21. /elders/subscriptions GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  // 23. 주간 통계 데이터 조회
  const sevenDaysAgo = new Date(new Date().setDate(new Date().getDate())).toISOString().slice(0, 10);
  res = http.get(`${BASE_URL}/elders/${ELDER_ID}/weekly-stats?startDate=${sevenDaysAgo}`, { headers: authHeaders });
  check(res, { '주간 통계 데이터 조회 상태 200 확인': (r) => r.status === 200 });
  if (res.status !== 200) console.log(`23. /elders/{elderId}/weekly-stats GET Status: ${res.status}, Body: ${res.body.substring(0, 200)}...`);
  sleep(1);

  sleep(Math.random() * 3);
}
