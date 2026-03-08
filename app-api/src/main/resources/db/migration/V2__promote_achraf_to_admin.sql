UPDATE users
SET role = 'ROLE_ADMIN'
WHERE email = 'achraf.zaime@gmail.com'
  AND role <> 'ROLE_ADMIN';
