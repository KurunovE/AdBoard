-- Пользователи
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),

    CONSTRAINT ak_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_name
        CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_users_email
        CHECK (email ~* '^[^@]+@[^@]+\.[^@]+$'),
    CONSTRAINT chk_users_phone
        CHECK (phone IS NULL OR phone ~ '^\+\d{1,3}\s?\(?\d+\)?[\d\s\-]+$')
);

-- Категории (иерархия через self-reference)
CREATE TABLE categories
(
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    parent_id BIGINT,

    CONSTRAINT fk_categories_parent_id
        FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL,

    CONSTRAINT ak_categories_name_parent
        UNIQUE (name, parent_id),

    CONSTRAINT chk_categories_name
        CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_categories_no_self_ref
        CHECK (parent_id != id)
);

-- Объявления
CREATE TABLE advertisements
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    price       NUMERIC(15, 2),
    status      advertisement_status    NOT NULL DEFAULT 'ACTIVE',
    author_id   BIGINT       NOT NULL,
    category_id BIGINT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_advertisements_author_id
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_advertisements_category_id
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,

    CONSTRAINT chk_advertisements_title
        CHECK (length(trim(title)) > 0),
    CONSTRAINT chk_advertisements_price
        CHECK (price IS NULL OR price >= 0),
    CONSTRAINT chk_advertisements_updated_at
        CHECK (updated_at >= created_at)
);

-- Фотографии объявлений
CREATE TABLE images
(
    id          BIGSERIAL PRIMARY KEY,
    advertisement_id       BIGINT       NOT NULL,
    url         VARCHAR(500) NOT NULL,
    path        VARCHAR(500) NOT NULL,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_images_advertisement_id
        FOREIGN KEY (advertisement_id) REFERENCES advertisements (id) ON DELETE CASCADE,

    CONSTRAINT ak_images_advertisement_sort
        UNIQUE (advertisement_id, sort_order),

    CONSTRAINT chk_images_url
        CHECK (length(trim(url)) > 0),
    CONSTRAINT chk_images_path
        CHECK (length(trim(path)) > 0),
    CONSTRAINT chk_images_sort_order
        CHECK (sort_order >= 0)
);

-- Комментарии
CREATE TABLE comments
(
    id         BIGSERIAL PRIMARY KEY,
    advertisement_id      BIGINT    NOT NULL,
    author_id  BIGINT    NOT NULL,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comments_ad_id
        FOREIGN KEY (advertisement_id) REFERENCES advertisements (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author_id
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT chk_comments_text
        CHECK (length(trim(text)) > 0)
);
