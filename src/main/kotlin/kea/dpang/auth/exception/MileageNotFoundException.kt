package kea.dpang.auth.exception

class MileageNotFoundException(userId: Long) : RuntimeException("해당 사용자의 마일리지 정보를 찾을 수 없습니다: $userId")