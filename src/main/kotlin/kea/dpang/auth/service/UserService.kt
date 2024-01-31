package kea.dpang.auth.service

import kea.dpang.auth.base.Role
import java.time.LocalDate

interface UserService {

    /**
     * 계정 등록 메서드.
     *
     * 이메일, 비밀번호, 권한을 기반으로 새로운 계정을 생성합니다.
     * 권한을 별도로 지정하지 않은 경우, 기본적으로 'USER' 권한을 부여합니다.
     * 지정된 권한이 있는 경우, 해당 권한으로 계정을 생성합니다.
     */
    fun register(email: String, password: String, role: Role, name: String, employeeNumber: String, joinDate: LocalDate)

    /**
     * 사용자 검증 메서드.
     *
     * 사용자의 이메일과 비밀번호를 입력받아 사용자를 검증합니다.
     * 검증에 성공한 경우, 사용자의 고유 식별자인 userIdx를 반환합니다.
     */
    fun verifyUser(email: String, password: String): Long

    /**
     * 비밀번호 재설정 요청 메서드.
     *
     * 사용자의 이메일을 입력받아 비밀번호 재설정을 위한 인증번호를 생성하고,
     * 이를 사용자의 이메일로 전송합니다.
     */
    fun requestPasswordReset(email: String)

    /**
     * 인증번호 검증 및 비밀번호 재설정 메서드.
     *
     * 사용자의 이메일, 인증번호, 새로운 비밀번호를 입력받아 인증번호를 검증하고,
     * 인증번호가 유효한 경우 사용자의 비밀번호를 재설정합니다.
     */
    fun resetPassword(email: String, code: String, newPassword: String)

    /**
     * 비밀번호 변경 메서드.
     *
     * 사용자의 이메일과 기존 비밀번호, 새로운 비밀번호를 입력받아 비밀번호를 변경합니다.
     */
    fun changePassword(email: String, oldPassword: String, newPassword: String)

    /**
     * 계정 삭제 메서드.
     *
     * 사용자의 고유 식별자를 기반으로 해당 사용자 계정을 삭제합니다.
     */
    fun deleteAccount(userId: Long)
}