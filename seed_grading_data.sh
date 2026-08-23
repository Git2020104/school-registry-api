#!/usr/bin/env bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== 1. Authenticating & Fetching JWT Token ==="
AUTH_RESP=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123", "email": "admin@skools.com"}')

TOKEN=$(echo "$AUTH_RESP" | jq -r '.accessToken // .token // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  AUTH_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "admin", "password": "password123"}')
  TOKEN=$(echo "$AUTH_RESP" | jq -r '.accessToken // .token // empty')
fi

echo "Token: ${TOKEN:0:25}..."
AUTH_HDR="Authorization: Bearer $TOKEN"

echo -e "\n=== 2. Creating Classes & Streams ==="
curl -s -X POST "$BASE_URL/classes" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"name": "Senior One", "code": "S1"}' | jq .

curl -s -X POST "$BASE_URL/streams" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"classId": 1, "name": "East"}' | jq .

echo -e "\n=== 3. Registering Students ==="
curl -s -X POST "$BASE_URL/students" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"admissionNumber": "S1/2026/001", "firstName": "John", "lastName": "Okello", "gender": "M", "dateOfBirth": "2010-05-12", "classId": 1, "streamId": 1}' | jq .

curl -s -X POST "$BASE_URL/students" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"admissionNumber": "S1/2026/002", "firstName": "Grace", "lastName": "Auma", "gender": "F", "dateOfBirth": "2010-08-20", "classId": 1, "streamId": 1}' | jq .

echo -e "\n=== 4. Creating O-Level Subjects ==="
curl -s -X POST "$BASE_URL/subjects" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"name": "Mathematics", "code": "456", "level": "O_LEVEL"}' | jq .

curl -s -X POST "$BASE_URL/subjects" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"name": "English Language", "code": "112", "level": "O_LEVEL"}' | jq .

curl -s -X POST "$BASE_URL/subjects" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"name": "Physics", "code": "535", "level": "O_LEVEL"}' | jq .

echo -e "\n=== 5. Creating Papers ==="
curl -s -X POST "$BASE_URL/papers" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"subjectId": 1, "paperNumber": 1, "name": "Mathematics Paper 1", "maxMarks": 100}' | jq .

curl -s -X POST "$BASE_URL/papers" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"subjectId": 1, "paperNumber": 2, "name": "Mathematics Paper 2", "maxMarks": 100}' | jq .

curl -s -X POST "$BASE_URL/papers" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"subjectId": 2, "paperNumber": 1, "name": "English Paper 1", "maxMarks": 100}' | jq .

echo -e "\n=== 6. Creating Exam Term ==="
curl -s -X POST "$BASE_URL/exam-terms" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"year": 2026, "term": 1, "name": "Term 1 2026"}' | jq .

echo -e "\n=== 7. Posting Marks (Single & Batch) ==="
curl -s -X POST "$BASE_URL/marks" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{"studentId": 1, "paperId": 1, "examTermId": 1, "score": 85.5, "remarks": "Excellent"}' | jq .

curl -s -X POST "$BASE_URL/marks/batch" -H "Content-Type: application/json" -H "$AUTH_HDR" \
  -d '{
    "paperId": 2,
    "examTermId": 1,
    "entries": [
      {"studentId": 1, "score": 78.0, "remarks": "Very Good"},
      {"studentId": 2, "score": 64.5, "remarks": "Good"}
    ]
  }' | jq .

echo -e "\n=== 8. Querying Student Marks ==="
curl -s -X GET "$BASE_URL/students/1/marks?termId=1" -H "$AUTH_HDR" | jq .

