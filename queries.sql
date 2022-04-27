select td.id, sqrt(sum(pow(td.frequency - tq.frequency, 2))) distance
from complete tq, complete td, query q
where td.name = tq.name and tq.id = q.id and q.label = "q1"
      and not td.id = any(select d.id 
			from document d, query q 
			where d.id = q.id)
group by td.id
order by distance asc;

select sqrt(sum(pow(td1.frequency - td2.frequency, 2))) distance
from complete td1, complete td2
where td1.id = 1 and td2.id = 4 and td1.name = td2.name;

select sum(td1.frequency * td2.frequency) distance
from complete td1, complete td2
where td1.id = 1 and td2.id = 4 and td1.name = td2.name;

select sum(td1.frequency * td2.frequency)/
	(sqrt(sum(pow(td1.frequency, 2))) * sqrt(sum(pow(td2.frequency, 2))))
	distance
from complete td1, complete td2
where td1.id = 1 and td2.id = 4 and td1.name = td2.name;

select distinct(id) 
from complete has
where not id = any(select d.id 
	from document d, query q 
	where d.id = q.id)
order by id;

select *
from complete has 
where not id = any(select d.id 
	from document d, query q 
	where d.id = q.id)
order by name, id;

select frequency
from complete has, query q
where has.id = q.id and q.label = "q1"
order by name;

select td.id, sqrt(sum(pow(td.frequency - tq.frequency, 2))) distance
from hassvd tq, hassvd td, query q
where td.termid = tq.termid and tq.id = q.id and q.label = "q1"
      and not td.id = any(select d.id 
			from document d, query q 
			where d.id = q.id)
group by td.id
order by distance asc;