-- advertisements
CREATE INDEX idx_advertisements_author_id ON advertisements (author_id);
CREATE INDEX idx_advertisements_category_id ON advertisements (category_id);
CREATE INDEX idx_advertisements_status ON advertisements (status);
CREATE INDEX idx_advertisements_created_at ON advertisements (created_at DESC);
CREATE INDEX idx_advertisements_price ON advertisements (price);

--category
CREATE INDEX idx_categories_parent_id ON categories (parent_id);

-- images
CREATE INDEX idx_images_ad_id ON images (advertisement_id);
CREATE INDEX idx_images_uploaded_at ON images (uploaded_at DESC);

-- comments
CREATE INDEX idx_comments_ad_id ON comments (advertisement_id);
CREATE INDEX idx_comments_author_id ON comments (author_id);
CREATE INDEX idx_comments_created_at ON comments (created_at DESC);
