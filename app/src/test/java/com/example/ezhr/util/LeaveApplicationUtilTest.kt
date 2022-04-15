package com.example.ezhr.util

import org.junit.Test
import com.google.common.truth.Truth.assertThat


class LeaveApplicationUtilTest {

    @Test
    fun `empty startDate returns false`() {
        val result = LeaveApplicationUtil.validateLeaveApplicationInput(
            startDate = "",
            endDate = "8-10-2022",
            leaveType = "Annual",
            leaveStatus = "Pending"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `empty endDate returns false`() {
        val result = LeaveApplicationUtil.validateLeaveApplicationInput (
            startDate = "5-10-2022",
            endDate = "",
            leaveType = "Medical",
            leaveStatus = "Pending"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `leave type not in leave category returns false`() {
        val result = LeaveApplicationUtil.validateLeaveApplicationInput (
            startDate = "5-10-2022",
            endDate = "8-10-2022",
            leaveType = "Holiday",
            leaveStatus = "Pending"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `leave status not in leave statuses return false`() {
        val result = LeaveApplicationUtil.validateLeaveApplicationInput (
            startDate = "5-10-2022",
            endDate = "8-10-2022",
            leaveType = "Annual",
            leaveStatus = "Terminated",
        )
        assertThat(result).isFalse()
    }
}