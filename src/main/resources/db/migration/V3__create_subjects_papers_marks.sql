CREATE TABLE subjects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    level VARCHAR(20) DEFAULT 'O_LEVEL'
);

CREATE TABLE papers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject_id INT NOT NULL,
    paper_number INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    max_marks INT DEFAULT 100,
    CONSTRAINT fk_paper_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT uq_subject_paper UNIQUE (subject_id, paper_number)
);

CREATE TABLE exam_terms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    "year" INT NOT NULL,
    "term" INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_year_term UNIQUE ("year", "term")
);

CREATE TABLE marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    paper_id INT NOT NULL,
    exam_term_id INT NOT NULL,
    score DOUBLE NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mark_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_mark_paper FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE,
    CONSTRAINT fk_mark_exam_term FOREIGN KEY (exam_term_id) REFERENCES exam_terms(id) ON DELETE CASCADE,
    CONSTRAINT uq_student_paper_term UNIQUE (student_id, paper_id, exam_term_id)
);