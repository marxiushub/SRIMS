# System Tests

## Overview

This document describes the manual system tests for the Ski Rental Inventory Management System.

The goal of these tests is to verify that the most important user flows work correctly from the frontend through the backend to the database.

---

## Test Environment

| Property | Value |
|---|---|
| Frontend | Angular application running locally |
| Backend | Spring Boot application running locally |
| Database | Local development/test database |
| Browser | Chrome / Firefox |
| Test Data | Generated via backend data generator |

---

## Test Users

| Role | Email | Password | Purpose |
|---|---|---|---|
| Customer | `hans.hansinger@email.com` | `password` | Used for customer profile and reservation tests |
| Staff/Admin | `admin@email.com` | `password` | Used for equipment and staff functionality |

---

## Test Result Values

| Result | Meaning |
|---|---|
| Passed | The test worked as expected |
| Failed | The test did not work as expected |
| Partially Passed | The main functionality worked, but something was missing or incorrect |
| Blocked | The test could not be executed because another required functionality was not working |
| Not tested | The test has not been executed yet |

---

## Test Summary

| Test ID | Test Case | Latest Result | Last Tested | Tester | Comment |
|---|---|---|---|---|---|
| ST-01 | Customer Login | Not tested | - | - | - |
| ST-02 | Staff Login | Not tested | - | - | - |
| ST-03 | Create Customer Profile | Not tested | - | - | - |
| ST-04 | Reject Invalid Customer Profile | Not tested | - | - | - |
| ST-05 | View Equipment Inventory | Not tested | - | - | - |
| ST-06 | Create Equipment as Staff | Not tested | - | - | - |
| ST-07 | Reject Invalid Equipment Creation | Not tested | - | - | - |
| ST-08 | Create Reservation | Not tested | - | - | - |
| ST-09 | Reject Reservation with Invalid Dates | Not tested | - | - | - |
| ST-10 | Search Reservations | Not tested | - | - | - |
| ST-11 | Add Equipment to Reservation | Not tested | - | - | - |
| ST-12 | Remove Equipment from Reservation | Not tested | - | - | - |
| ST-13 | Delete Reservation | Not tested | - | - | - |
| ST-14 | Unauthorized Access Is Blocked | Not tested | - | - | - |
| ST-15 | Logout | Not tested | - | - | - |

---

## ST-01: Customer Login

### Goal

Verify that an existing customer can log in successfully.

### Preconditions

- Backend is running.
- Frontend is running.
- Customer user exists in the database.

### Test Steps

1. Open the frontend in the browser.
2. Navigate to the login page.
3. Enter the customer email: `hans.hansinger@email.com`.
4. Enter the password: `password`.
5. Click the login button.

### Expected Result

- The login is successful.
- The customer is redirected to the customer area.
- Customer-specific functionality is accessible.
- No error message is shown.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-02: Staff Login

### Goal

Verify that a staff/admin user can log in successfully.

### Preconditions

- Backend is running.
- Frontend is running.
- Staff user exists in the database.

### Test Steps

1. Open the frontend in the browser.
2. Navigate to the login page.
3. Enter the staff email: `admin@email.com`.
4. Enter the password: `password`.
5. Click the login button.

### Expected Result

- The login is successful.
- The staff user is redirected to the staff area.
- Staff-only functionality is visible.
- No error message is shown.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-03: Create Customer Profile

### Goal

Verify that a logged-in customer can create a new customer profile.

### Preconditions

- Customer is logged in.
- Customer has access to the profile management page.

### Test Steps

1. Navigate to the customer profile page.
2. Click the button to create a new profile.
3. Enter a profile name.
4. Enter height, weight and shoe size.
5. Select a skill level.
6. Submit the form.

### Expected Result

- The profile is created successfully.
- The new profile appears in the profile list.
- The entered data is displayed correctly.
- No validation error is shown.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-04: Reject Invalid Customer Profile

### Goal

Verify that invalid customer profile data is rejected.

### Preconditions

- Customer is logged in.
- Customer is on the create profile page.

### Test Steps

1. Open the create profile form.
2. Leave the profile name empty.
3. Fill in the remaining fields with valid values.
4. Submit the form.

### Expected Result

- The profile is not created.
- A validation error is shown.
- The user remains on the form.
- No invalid profile appears in the profile list.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-05: View Equipment Inventory

### Goal

Verify that equipment can be loaded and displayed.

### Preconditions

- User is logged in.
- Equipment exists in the database.

### Test Steps

1. Navigate to the equipment or inventory page.
2. Wait until the equipment list is loaded.

### Expected Result

- The equipment list is displayed.
- Each item shows relevant information such as model/name, price and status.
- No loading or server error is shown.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-06: Create Equipment as Staff

### Goal

Verify that staff can create new equipment.

### Preconditions

- Staff user is logged in.
- Staff user has permission to create equipment.

### Test Steps

1. Navigate to the staff inventory page.
2. Click the button to create new equipment.
3. Fill in all required equipment fields.
4. Submit the form.

### Expected Result

- The equipment is created successfully.
- The new equipment appears in the inventory list.
- The entered values are displayed correctly.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-07: Reject Invalid Equipment Creation

### Goal

Verify that invalid equipment data is rejected.

### Preconditions

- Staff user is logged in.
- Staff user is on the create equipment page.

### Test Steps

1. Open the create equipment form.
2. Leave a required field empty or enter invalid data.
3. Submit the form.

### Expected Result

- The equipment is not created.
- A validation error is shown.
- The user remains on the form.
- No invalid equipment appears in the inventory list.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-08: Create Reservation

### Goal

Verify that a customer can create a reservation for available equipment.

### Preconditions

- Customer is logged in.
- Customer has at least one customer profile.
- Equipment is available.
- Selected reservation dates are valid.

### Test Steps

1. Navigate to the reservation page.
2. Select a customer profile.
3. Select available equipment.
4. Choose a valid start date and end date.
5. Select a pickup time.
6. Submit the reservation.

### Expected Result

- The reservation is created successfully.
- The reservation status is `CREATED`.
- The selected equipment appears in the reservation.
- The total price is calculated correctly.
- The reservation is visible in the reservation overview.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-09: Reject Reservation with Invalid Dates

### Goal

Verify that a reservation with invalid dates is rejected.

### Preconditions

- Customer is logged in.
- Customer has at least one customer profile.
- Equipment is available.

### Test Steps

1. Navigate to the reservation page.
2. Select a customer profile.
3. Select available equipment.
4. Choose an end date that is before the start date.
5. Submit the reservation.

### Expected Result

- The reservation is not created.
- A validation error is shown.
- The user remains on the reservation form.
- No invalid reservation appears in the overview.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-10: Search Reservations

### Goal

Verify that reservations can be searched and filtered.

### Preconditions

- At least one reservation exists.
- User is logged in with permission to view reservations.

### Test Steps

1. Navigate to the reservation overview.
2. Enter search/filter criteria, for example customer profile, date range or reservation status.
3. Start the search.

### Expected Result

- Only matching reservations are displayed.
- Non-matching reservations are not displayed.
- If no reservation matches, an empty result list is shown.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-11: Add Equipment to Reservation

### Goal

Verify that equipment can be added to an existing reservation.

### Preconditions

- A reservation exists.
- Additional equipment is available for the reservation period.
- User has permission to update reservations.

### Test Steps

1. Open an existing reservation.
2. Choose additional available equipment.
3. Add the equipment to the reservation.

### Expected Result

- The equipment is added to the reservation.
- The reservation details show the new equipment.
- The total price is updated correctly.
- The equipment is blocked for the reservation period.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-12: Remove Equipment from Reservation

### Goal

Verify that equipment can be removed from an existing reservation.

### Preconditions

- A reservation exists.
- The reservation contains at least two equipment items.
- User has permission to update reservations.

### Test Steps

1. Open an existing reservation.
2. Select one equipment item from the reservation.
3. Remove the selected item.

### Expected Result

- The equipment is removed from the reservation.
- The reservation still exists.
- The total price is updated correctly.
- The removed equipment is available again for the reservation period.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-13: Delete Reservation

### Goal

Verify that a reservation can be deleted.

### Preconditions

- A reservation exists.
- User has permission to delete reservations.

### Test Steps

1. Open the reservation overview.
2. Select an existing reservation.
3. Trigger the delete action.
4. Confirm the deletion.

### Expected Result

- The reservation is deleted.
- The reservation no longer appears in the overview.
- Related blocked time periods for the equipment are removed.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-14: Unauthorized Access Is Blocked

### Goal

Verify that users cannot access functionality without the required permissions.

### Preconditions

- A customer user is logged in.
- Staff-only functionality exists.

### Test Steps

1. Log in as customer.
2. Try to access a staff-only page, for example equipment creation or staff management.

### Expected Result

- Access is denied.
- The user is redirected or an error message is shown.
- Staff-only data cannot be modified.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |

---

## ST-15: Logout

### Goal

Verify that a logged-in user can log out successfully.

### Preconditions

- User is logged in.

### Test Steps

1. Click the logout button.
2. Try to access a protected page afterwards.

### Expected Result

- The user is logged out.
- The authentication token is removed or invalidated on the client side.
- Protected pages are no longer accessible without logging in again.

### Test Execution

| Date | Tester | Result | Actual Result | Issue / Comment |
|---|---|---|---|---|
| YYYY-MM-DD | Name | Not tested | - | - |