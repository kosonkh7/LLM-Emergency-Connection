import sqlite3
import csv
import pandas as pd

# SQLite 데이터베이스 연결
conn = sqlite3.connect('em.db')
cursor = conn.cursor()

data = pd.read_csv("data.csv")
data.columns

# CSV 파일 경로
csv_file = 'data.csv'

# CSV 파일 열기
with open(csv_file, mode='r', encoding='utf-8') as file:
    csv_reader = csv.reader(file)
    
    # 헤더를 건너뛰고 데이터만 처리
    next(csv_reader)  # 첫 번째 줄은 헤더이므로 건너뜀
    
    # 각 행을 테이블에 삽입
    for row in csv_reader:
        cursor.execute('''
            INSERT INTO emergency (id, address, emergency_medical_institution_type, hospital_name,
            latitude, longitude, phone_number1, phone_number3, region) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', row)  # row의 각 요소가 순서대로 들어갑니다.

# 변경 사항 저장
conn.commit()

conn.close()
