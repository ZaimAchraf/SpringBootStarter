UPDATE users
SET role = 'ROLE_ADMIN'
WHERE email = 'achraf@test.com'
  AND role <> 'ROLE_ADMIN';
