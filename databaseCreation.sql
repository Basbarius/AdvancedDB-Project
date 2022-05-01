drop table has;
drop table hassvd;
drop table text;
drop table query;
drop table document;
drop table word;
drop table term;


create table document(
	id int auto_increment, 
	primary key (id));
create table text(
	id int, 
	url char(300),
	title char(40), 
	author char(50), 
	date date,
	primary key (url),
	foreign key (id) references document (id)
);
create table query(
	id int,
	label char(10),
	primary key (label),
	foreign key (id) references document (id)
);
create table term(
	name char(20),
	primary key (name)
);
create table word(
	word char(30),
	name char (20),
	primary key (word),
	foreign key (name) references term (name)
);
create table has(
	id int,
	name char(20),
	frequency int, 
	foreign key (id) references document (id),
	foreign key (name) references term (name),
	primary key (id, name)
);
create table hasSVD(
	id int,
	termid int,
	frequency real,
	foreign key (id) references document (id),
	primary key(id, termid)
);

create view complete as
	(select id, name, frequency
	from has)
	union
	(select id, name, 0
	from document, term
	where (id, name) not in (select id, name from has));


