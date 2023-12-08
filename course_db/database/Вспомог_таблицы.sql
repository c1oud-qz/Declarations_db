create TEMPORARY TABLE temp_results AS
SELECT декларации.id_ДТ, профили_риска.№ПРполн as профил from декларации
INNER JOIN
профили_риска ON (декларации.код_ТНВЭД = профили_риска.товар) AND (декларации.страна_проис = профили_риска.страна)
AND (декларации.дата_подачи < профили_риска.датаОконч) AND left(профили_риска.создавшийТО, 3)=left(декларации.кодТО, 3);

START TRANSACTION;
UPDATE декларации
JOIN temp_results ON декларации.id_ДТ = temp_results.id_ДТ
SET декларации.профиль_риска = temp_results.профил;
-- SELECT * from декларации where профиль_риска<>'';

create temporary table temp_TO as
select * from перечень_тстк
inner join декларации on (перечень_тстк.код_ТО_размещ = декларации.кодТО)
inner join профили_риска on (перечень_тстк.вид = профили_риска.испТСТК)
where профиль_риска is not null and профили_риска.№ПРполн=декларации.профиль_риска;


select название, технич_номер, кодТО, испТСТК, допТСТК from temp_TO;
ROLLBACK;

DROP TEMPORARY TABLE IF EXISTS temp_results;
DROP TEMPORARY TABLE IF EXISTS temp_TO;
