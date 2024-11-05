CREATE TABLE IF NOT EXISTS PostTest (
                                    id INT NOT NULL,
                                    userid INT NOT NULL,
                                    title varchar(250) NOT NULL,
                                    body text NOT NULL,
                                    version int,
                                    PRIMARY KEY (id)
);