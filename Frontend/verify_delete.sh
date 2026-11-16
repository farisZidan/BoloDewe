#!/bin/bash

# Skrip Verifikasi Log DELETE Produk
# Mengetes rute endpoint pada sisi server untuk menghapus ID produk,
# serta melihat response body dan status code agar sinkron dengan hasil query database.

BASE_URL="http://home-lab-server.web.id"
TOKEN_JWT="MASUKKAN_TOKEN_DI_SINI"

ID_PRODUK=${1:-1}

echo "=========================================================="
echo "Memulai Verifikasi Endpoint DELETE Produk"
echo "Target Endpoint: DELETE $BASE_URL/api/products/$ID_PRODUK"
echo "=========================================================="
echo ""

HTTP_STATUS=$(curl -s -w "%{http_code}" -X DELETE \
  "$BASE_URL/api/products/$ID_PRODUK" \
  -H "Authorization: Bearer $TOKEN_JWT" \
  -o /tmp/delete_resp.txt)

echo "-> Status HTTP: $HTTP_STATUS"
echo "-> Response Body:"
cat /tmp/delete_resp.txt
echo ""
echo ""

if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 204 ]; then
  echo "✅ BERHASIL: Rute merespons dengan status $HTTP_STATUS."
elif [ "$HTTP_STATUS" -eq 401 ]; then
  echo "❌ GAGAL: Unauthorized (401). Header JWT tidak dikenali."
elif [ "$HTTP_STATUS" -eq 404 ]; then
  echo "⚠️ GAGAL: Not Found (404). ID produk tidak ada dalam database server."
elif [ "$HTTP_STATUS" -eq 405 ]; then
  echo "❌ GAGAL: Method Not Allowed (405). Pastikan rute DELETE /api/products/{id} siap di sisi server."
elif [ "$HTTP_STATUS" -ge 500 ]; then
  echo "❌ GAGAL: Internal Server Error ($HTTP_STATUS). Ada masalah eksekusi query database."
else
  echo "⚠️ UNKNOWN: Mendapat status aneh HTTP $HTTP_STATUS. Harap periksa log server."
fi
echo ""
echo "=========================================================="
