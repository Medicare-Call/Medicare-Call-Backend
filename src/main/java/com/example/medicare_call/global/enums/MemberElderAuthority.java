package com.example.medicare_call.global.enums;

/**
 * Member와 Elder 사이 권한을 정의하는 Enum.
 * Elder 생성자는 MANAGE 권한을, 공유받은 사용자는 VIEW 권한을 갖는다.
 */
public enum MemberElderAuthority {
    MANAGE,
    VIEW
}
