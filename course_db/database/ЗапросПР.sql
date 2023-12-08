create TEMPORARY TABLE temp_results AS
SELECT декларации.id_ДТ, профили_риска.№ПРполн as профил from декларации
INNER JOIN
    профили_риска ON (декларации.код_ТНВЭД = профили_риска.товар) AND (декларации.страна_проис = профили_риска.страна)
    AND (декларации.дата_подачи < профили_риска.датаОконч) AND left(профили_риска.создавшийТО, 3)=left(декларации.кодТО, 3);

START TRANSACTION;    
UPDATE декларации
JOIN temp_results ON декларации.id_ДТ = temp_results.id_ДТ
SET декларации.профиль_риска = temp_results.профил;
SELECT * from декларации
ROLLBACK;

DROP TEMPORARY TABLE IF EXISTS temp_results;
