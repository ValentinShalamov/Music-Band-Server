CREATE TABLE IF NOT EXISTS owners (
	owner_id integer GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
	login varchar(64) NOT NULL,
	pass text NOT NULL
);

CREATE TABLE IF NOT EXISTS music_bands (
	band_id bigint GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
	name text NOT NULL,
	genre varchar(32) NOT NULL,
	number_participants integer NOT NULL,
	creation_date varchar(64) NOT NULL,
	establishment_date DATE NOT NULL,
	best_album_name text NOT NULL,
	best_album_sales bigint NOT NULL,
	owner_id integer NOT NULL,

	CONSTRAINT FK_band_owner_id FOREIGN KEY (owner_id)
	REFERENCES owners(owner_id)
);
