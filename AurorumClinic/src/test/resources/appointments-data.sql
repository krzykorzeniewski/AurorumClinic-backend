insert into user_ (name, surname, pesel, birthdate, email, phone_number, two_factor_authentication, role, password, created_at) values
                                                                                                                                    ('Michal', 'Laskowski', '12121212121', '1990-01-01', 's27626@pjwstk.edu.pl', '887153106' , 0, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa', getdate()),
                                                                                                                                    ('Piotr', 'Zieliński', '92030498765', '1992-03-04', 'piotr.zielinski@example.com', '601234569', 0, 'DOCTOR', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa', getdate()),
                                                                                                                                    ('Ewa', 'Wiśniewska', '93040567891', '1993-04-05', 'ewa.wisniewska@example.com', '601234570', 0, 'DOCTOR', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa', getdate()),
                                                                                                                                    ('Tomasz', 'Lewandowski', '95060712345', '1995-06-07', 'tomasz.lewandowski@example.com', '601234571', 1, 'PATIENT', '$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa', getdate());


insert into patient (pk_patient, communication_preferences, newsletter) values
                                                                            (1, 'EMAIL', 0),
                                                                            (4, 'EMAIL', 0);


insert into doctor (pk_doctor, description, profile_picture, education, experience, pwz_number) values
                                                                                                    (2, 'Pracuję indywidualnie z osobami dorosłymi w nurcie terapii poznawczo-behawioralnej.', null, 'mgr psychologii', 'doświadczenie zawodowe zdobyte na stażach w IPiN w Warszawie', null),
                                                                                                    (3, 'Psychiatra dorosłych specjalizujący się w leczeniu zaburzeń dwubiegunowych oraz depresji', null, 'Pomorski Uniwersytet Medyczny w Szczecinie', 'Centrum Zdrowia Psychicznego w Słupsku', 'PWZ3318632');


insert into specialization (name) values
                                      ('Psycholog dorosłych'),
                                      ('Psychiatra dorosłych');


insert into specialization_doctor (pk_doctor, pk_specialization) values
                                                                     (2, 1),
                                                                     (3, 2);


insert into schedule (started_at, finished_at, fk_doctor) values
                                                              ('2025-11-24T08:00:00', '2025-11-24T17:00:00', 2),
                                                              ('2025-11-25T08:00:00', '2025-11-25T17:00:00', 2),
                                                              ('2025-11-24T10:00:00', '2025-11-24T20:00:00', 3),
                                                              ('2025-11-25T08:00:00', '2025-11-25T20:00:00', 3);


insert into service (name, duration, price, description) values
                                                             ('Konsultacja psychiatryczna dorosłych (pierwsza wizyta)', 30, 350, 'Ocena stanu psychicznego z indywidualnym planem leczenia'),
                                                             ('Konsultacja psychologiczna dorosłych (pierwsza wizyta)', 60, 250, 'Krótka ocena psychologiczna, diagnoza oraz wstępny plan terapeutyczny');


insert into specialization_service (pk_service, pk_specialization) values
                                                                       (1, 2),
                                                                       (2, 1);


insert into service_schedule (pk_schedule, pk_service) values
                                                           (1, 2),
                                                           (2, 2),
                                                           (3, 1),
                                                           (4, 1);


insert into appointment (started_at, finished_at, status, description, fk_service, fk_opinion, fk_doctor, fk_patient, notification_sent, fk_payment) values
                                                                                                                                                         ('2025-11-24T08:00:00', '2025-11-24T08:30:00', 'CREATED', 'odczuwam chroniczny stres i mam napady lękowe', 2, null, 2, 1, 0, null),
                                                                                                                                                         ('2025-11-25T10:00:00', '2025-11-25T10:30:00', 'FINISHED', 'chciałbym sobię zapalić nieco trawkę', 2, null, 2, 1, 1, null),
                                                                                                                                                         ('2025-11-24T13:00:00', '2025-11-24T14:00:00', 'CREATED', 'chłop za dużo gra w robloxa', 1, null, 3, 4, 0, null),
                                                                                                                                                         ('2025-11-25T08:00:00', '2025-11-25T09:00:00', 'FINISHED', 'widzę podwójnie', 1, null, 3, 4, 1, null);