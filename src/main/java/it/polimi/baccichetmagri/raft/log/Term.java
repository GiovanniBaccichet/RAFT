package it.polimi.baccichetmagri.raft.log;

import java.io.Serializable;
import java.util.Objects;

public class Term implements Serializable, Comparable<Term> {

    // Term ID
    private final int termNumber;

    // Create next term
    public Term next() {
        return new Term(termNumber + 1);
    }

    public Term(int number) {
        this.termNumber = number;
    }

    @Override
    public int compareTo(Term comparingTerm) {
        return termNumber - comparingTerm.termNumber;
    }

    public boolean isLessThan(Term comparingTerm) {
        return compareTo(comparingTerm) < 0;
    }

    public boolean isGreaterThan(Term comparingTerm) {
        return compareTo(comparingTerm) > 0;
    }

    public int getTermNumber() {
        return termNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Term term = (Term) o;
        return termNumber == term.termNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(termNumber);
    }

    @Override
    public String toString() {
        return "Term{" +
                "termNumber=" + termNumber +
                '}';
    }

}
