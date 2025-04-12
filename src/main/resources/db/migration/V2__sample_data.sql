-- Insert sample users
INSERT INTO users (username, email, first_name, last_name)
VALUES 
    ('user1', 'user1@example.com', 'John', 'Doe'),
    ('user2', 'user2@example.com', 'Jane', 'Smith'),
    ('admin', 'admin@example.com', 'Admin', 'User');

-- Insert sample telephone numbers
INSERT INTO telephone_numbers (number, country_code, area_code, number_type, category, features, status, batch_id)
VALUES 
    ('+12025550001', '1', '202', 'MOBILE', 'STANDARD', 'SMS,Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550002', '1', '202', 'MOBILE', 'PREMIUM', 'SMS,Voice,Data', 'AVAILABLE', 'sample-batch'),
    ('+12025550003', '1', '202', 'LANDLINE', 'STANDARD', 'Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550004', '1', '202', 'MOBILE', 'STANDARD', 'SMS,Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550005', '1', '202', 'MOBILE', 'PREMIUM', 'SMS,Voice,Data', 'AVAILABLE', 'sample-batch'),
    ('+12025550006', '1', '202', 'LANDLINE', 'STANDARD', 'Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550007', '1', '202', 'MOBILE', 'STANDARD', 'SMS,Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550008', '1', '202', 'MOBILE', 'PREMIUM', 'SMS,Voice,Data', 'AVAILABLE', 'sample-batch'),
    ('+12025550009', '1', '202', 'LANDLINE', 'STANDARD', 'Voice', 'AVAILABLE', 'sample-batch'),
    ('+12025550010', '1', '202', 'MOBILE', 'STANDARD', 'SMS,Voice', 'AVAILABLE', 'sample-batch');

-- Insert sample file upload
INSERT INTO file_uploads (file_name, original_file_name, file_size, content_type, batch_id, status, total_records, processed_records, failed_records, uploaded_by)
VALUES 
    ('sample-batch.csv', 'numbers.csv', 1024, 'text/csv', 'sample-batch', 'COMPLETED', 10, 10, 0, 'admin');
