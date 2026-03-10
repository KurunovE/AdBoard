-- Автообновление updated_at при каждом UPDATE
CREATE OR REPLACE FUNCTION fn_advertisements_set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_advertisements_set_updated_at
    BEFORE UPDATE
    ON advertisements
    FOR EACH ROW
EXECUTE FUNCTION fn_advertisements_set_updated_at();

--Запрет удаления категории с привязанными объявлениями
CREATE OR REPLACE FUNCTION fn_categories_prevent_delete_if_used()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS (SELECT 1 FROM advertisements WHERE category_id = OLD.id) THEN
        RAISE EXCEPTION
            'Невозможно удалить категорию id=% "%", так как к ней привязаны объявления',
            OLD.id, OLD.name;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_categories_prevent_delete_if_used
    BEFORE DELETE
    ON categories
    FOR EACH ROW
EXECUTE FUNCTION fn_categories_prevent_delete_if_used();