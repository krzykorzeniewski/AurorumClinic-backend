--users
insert into user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password) values
    ('Michal', 'Laskowski', '12121212121', '1990-01-01', 's27626@pjwstk.edu.pl', '887153106' , 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Anna', 'Kowalska', '90010112345', '1990-01-01', 'anna.kowalska@example.com', '601234567', 0, 'ADMIN', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Marcin', 'Nowak', '85051234567', '1985-05-12', 'marcin.nowak@example.com', '601234568', 0, 'EMPLOYEE', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Piotr', 'Zieliński', '92030498765', '1992-03-04', 'piotr.zielinski@example.com', '601234569', 0, 'DOCTOR', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Ewa', 'Wiśniewska', '93040567891', '1993-04-05', 'ewa.wisniewska@example.com', '601234570', 0, 'DOCTOR', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Tomasz', 'Lewandowski', '95060712345', '1995-06-07', 'tomasz.lewandowski@example.com', '601234571', 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Anna', 'Wójcik', '87080934567', '1987-08-09', 'anna.wojcik@example.com', '601234572', 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Kamil', 'Majewski', '86011056789', '1986-01-10', 'kamil.majewski@example.com', '601234573', 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Maria', 'Jankowska', '80021234567', '1980-02-12', 'maria.jankowska@example.com', '601234574', 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Paweł', 'Kowalczyk', '87041567890', '1987-04-15', 'pawel.kowalczyk@example.com', '601234575', 0, 'DOCTOR', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

INSERT INTO user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password)
VALUES ('Dorota', 'Szymczak', '82061712345', '1982-06-17', 'dorota.szymczak@example.com', '601234576', 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa');

update user_ set email_verified = 1;
update user_ set phone_number_verified = 0;

--patients
insert into patient (pk_patient, communication_preferences, newsletter) values (1, 'EMAIL', 0);
insert into patient (pk_patient, communication_preferences, newsletter) values (6, 'EMAIL', 0);
insert into patient (pk_patient, communication_preferences, newsletter) values (7, 'EMAIL', 0);
insert into patient (pk_patient, communication_preferences, newsletter) values (8, 'EMAIL', 0);
insert into patient (pk_patient, communication_preferences, newsletter) values (9, 'EMAIL', 0);
insert into patient (pk_patient, communication_preferences, newsletter) values (11, 'EMAIL', 0);

--doctors
insert into doctor (pk_doctor, description, profile_picture, education, experience, pwz_number) values
    (4, 'Pracuję indywidualnie z osobami dorosłymi w nurcie terapii poznawczo-behawioralnej.', null, 'mgr psychologii', 'doświadczenie zawodowe zdobyte na stażach w IPiN w Warszawie', null);
insert into doctor (pk_doctor, description, profile_picture, education, experience, pwz_number) values
    (5, 'Psychiatra dorosłych specjalizujący się w leczeniu zaburzeń dwubiegunowych oraz depresji', null, 'Pomorski Uniwersytet Medyczny w Szczecinie', 'Centrum Zdrowia Psychicznego w Słupsku', 'PWZ3318632');
insert into doctor (pk_doctor, description, profile_picture, education, experience, pwz_number) values
    (10, 'Psychiatra dziecięcy pracujący z najmłodszymi', null, 'Uniwersytet Pomorski w Słupsku (chyba nie mają medycyny ale esz)', 'POZ na Piłsudskiego w Słupsku', 'PWZ123532');

--specializations
insert into specialization (name) values ('Psycholog dorosłych');
insert into specialization (name) values ('Psycholog dziecięcy');
insert into specialization (name) values ('Psychiatra dorosłych');
insert into specialization (name) values ('Psychiatra dziecięcy');
insert into specialization (name) values ('Psychoterapeuta');

insert into specialization_doctor (pk_doctor, pk_specialization) values (4, 1);
insert into specialization_doctor (pk_doctor, pk_specialization) values (4, 5);
insert into specialization_doctor (pk_doctor, pk_specialization) values (5, 3);
insert into specialization_doctor (pk_doctor, pk_specialization) values (10, 4);

--schedules
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-04T08:00:00', '2025-10-04T17:00:00', 4);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-05T08:00:00', '2025-10-05T17:00:00', 4);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-06T14:00:00', '2025-10-06T21:00:00', 4);

insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-04T10:00:00', '2025-10-04T20:00:00', 5);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-05T08:00:00', '2025-10-05T17:00:00', 5);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-06T10:00:00', '2025-10-06T20:00:00', 5);

insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-04T09:00:00', '2025-10-04T21:00:00', 10);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-05T09:00:00', '2025-10-05T21:00:00', 10);
insert into schedule (started_at, finished_at, fk_doctor) values ('2025-10-06T09:00:00', '2025-10-06T21:00:00', 10);

--services
insert into service (name, duration, price, description) values ('Konsultacja psychiatryczna dorosłych (pierwsza wizyta)', 45, 350, 'Ocena stanu psychicznego z indywidualnym planem leczenia');
insert into service (name, duration, price, description) values ('Konsultacja psychiatryczna dzieci (pierwsza wizyta)', 45, 350, 'Ocena stanu psychicznego z indywidualnym planem leczenia');
insert into service (name, duration, price, description) values ('Konsultacja psychiatryczna dorosłych (kolejna wizyta)', 30, 300, 'Weryfikacja postępów, modyfikacja terapii oraz dalsze planowanie opieki');
insert into service (name, duration, price, description) values ('Konsultacja psychiatryczna dzieci (kolejna wizyta)', 30, 300, 'Weryfikacja postępów, modyfikacja terapii oraz dalsze planowanie opieki');
insert into service (name, duration, price, description) values ('Konsultacja psychologiczna dorosłych (pierwsza wizyta)', 60, 250, 'Krótka ocena psychologiczna, diagnoza oraz wstępny plan terapeutyczny');
insert into service (name, duration, price, description) values ('Konsultacja psychologiczna dorosłych (kolejna wizyta)', 60, 200, 'Kontynuacja terapii');

insert into specialization_service (pk_service, pk_specialization) values (1, 3);
insert into specialization_service (pk_service, pk_specialization) values (3, 3);
insert into specialization_service (pk_service, pk_specialization) values (5, 1);
insert into specialization_service (pk_service, pk_specialization) values (6, 1);
insert into specialization_service (pk_service, pk_specialization) values (2, 4);
insert into specialization_service (pk_service, pk_specialization) values (4, 4);

insert into service_schedule(pk_schedule, pk_service) values (1, 5);
insert into service_schedule(pk_schedule, pk_service) values (1, 6);
insert into service_schedule(pk_schedule, pk_service) values (2, 5);
insert into service_schedule(pk_schedule, pk_service) values (2, 6);
insert into service_schedule(pk_schedule, pk_service) values (3, 5);
insert into service_schedule(pk_schedule, pk_service) values (3, 6);

insert into service_schedule(pk_schedule, pk_service) values (4, 1);
insert into service_schedule(pk_schedule, pk_service) values (4, 3);
insert into service_schedule(pk_schedule, pk_service) values (5, 1);
insert into service_schedule(pk_schedule, pk_service) values (5, 3);
insert into service_schedule(pk_schedule, pk_service) values (6, 1);
insert into service_schedule(pk_schedule, pk_service) values (6, 3);

insert into service_schedule(pk_schedule, pk_service) values (7, 2);
insert into service_schedule(pk_schedule, pk_service) values (7, 4);
insert into service_schedule(pk_schedule, pk_service) values (8, 2);
insert into service_schedule(pk_schedule, pk_service) values (8, 4);
insert into service_schedule(pk_schedule, pk_service) values (9, 2);
insert into service_schedule(pk_schedule, pk_service) values (9, 4);


--opinions
insert into opinion (rating, comment, answer, created_at) values (5, 'Bardzo miły psycholog', null, '2025-10-04T21:27:00');
insert into opinion (rating, comment, answer, created_at) values (2, 'Lekarz przepisał lekarstwa, następnie poszedłem wykupić lekarstwa, gdzie Pani w aptece poinformowała mnie, że łączenie dwóch leków może wywołać interakcje. Nie kupiłem tych lekarstw. Napisałem do lekarza odpisał mi: dlaczego?', null, '2025-10-05T17:29:10');
insert into opinion (rating, comment, answer, created_at) values (4, 'OK.', null, '2025-10-06T07:17:06');

--appointments
insert into appointment (started_at, finished_at, status, description, fk_service, fk_opinion, fk_doctor, fk_patient, notification_sent)
values ('2025-10-04T08:00:00', '2025-10-04T09:00:00', 'FINISHED', 'odczuwam chroniczny stres i mam napady lękowe', 5, 1, 4, 1, 1);

insert into appointment (started_at, finished_at, status, description, fk_service, fk_opinion, fk_doctor, fk_patient, notification_sent)
values ('2025-10-04T10:00:00', '2025-10-04T10:40:00', 'FINISHED', 'chciałbym sobię zapalić nieco trawkę', 1, 2, 5, 8, 1);

insert into appointment (started_at, finished_at, status, description, fk_service, fk_opinion, fk_doctor, fk_patient, notification_sent)
values ('2025-10-04T13:00:00', '2025-10-04T13:30:00', 'FINISHED', 'chłop za dużo gra w robloxa', 4, 3, 10, 9, 1);

--payments
insert into payment (amount, created_at ,completed_at, method, status, fk_appointment) values (250, '2025-10-04T07:51:52', '2025-10-04T07:52:31', 'OFFLINE', 'COMPLETED', 1);
insert into payment (amount, created_at ,completed_at, method, status, fk_appointment) values (350, '2025-10-04T09:31:52', '2025-10-04T09:58:18', 'OFFLINE', 'COMPLETED', 2);
insert into payment (amount, created_at ,completed_at, method, status, fk_appointment) values (300, '2025-10-04T12:31:52' ,'2025-10-04T12:31:52', 'BLIK', 'COMPLETED', 3);