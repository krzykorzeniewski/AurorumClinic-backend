alter procedure AppointmentSlots @StartedAt datetime2(5), @FinishedAt datetime2(5),
    @PkService int, @PkDoctor int as
begin
declare @ServiceDuration int;
create table #appoinment_slots (
                                   timeslot datetime2(5)
);
select @ServiceDuration = duration from service where pk_service = @PkService;
while @StartedAt < @FinishedAt
begin
            if not exists (
                select 1 from appointment a
                          join doctor d on d.pk_doctor = a.fk_doctor
                where a.started_at < dateadd(minute, @ServiceDuration, @StartedAt)
                    and a.finished_at > @StartedAt
                    and d.pk_doctor = @PkDoctor
            ) and exists (
                select 1 from schedule s
                         join doctor d on d.pk_doctor = s.fk_doctor
                         join service_schedule s2 on s2.pk_schedule = s.pk_schedule
                where s.started_at < dateadd(minute, @ServiceDuration, @StartedAt)
                    and s.finished_at > @StartedAt
                    and d.pk_doctor = @PkDoctor
            )
            insert into #appoinment_slots values (@StartedAt);
            set @StartedAt = dateadd(minute, @ServiceDuration, @StartedAt)
end
select * from #appoinment_slots;
end