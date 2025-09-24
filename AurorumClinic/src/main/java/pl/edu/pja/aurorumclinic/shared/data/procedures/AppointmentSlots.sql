alter procedure AppointmentSlots @StartedAt datetime2(5), @FinishedAt datetime2(5),
    @ServiceDuration int, @PkDoctor int as
begin
create table #appoinment_slots (
                                   timeslot datetime2(5)
);
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
                where s.started_at < dateadd(minute, @ServiceDuration, @StartedAt)
                    and s.finished_at > @StartedAt
                    and d.pk_doctor = @PkDoctor
            )
            insert into #appoinment_slots values (@StartedAt);
            set @StartedAt = dateadd(minute, @ServiceDuration, @StartedAt)
end
select * from #appoinment_slots;
end