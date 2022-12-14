package pl.patrykkawula.library.app;

import pl.patrykkawula.library.exception.*;
import pl.patrykkawula.library.io.ConsolePrinter;
import pl.patrykkawula.library.io.DataReader;
import pl.patrykkawula.library.io.file.FileManager;
import pl.patrykkawula.library.io.file.FileManagerBuilder;
import pl.patrykkawula.library.model.*;

import java.util.Comparator;
import java.util.InputMismatchException;

public class LibraryControl {
    ConsolePrinter printer = new ConsolePrinter();

    DataReader dataReader = new DataReader(printer);
    private FileManager fileManager;

    Library library;

    LibraryControl() {
        fileManager = new FileManagerBuilder(printer, dataReader).build();
        {
            try {
                library = fileManager.importData();
                printer.printLine("Zaimportowano dane z pliku");
            } catch (DataImportException | InvalidDataException e) {
                printer.printLine(e.getMessage());
                printer.printLine("Zainicjowano nową bazę");
                library = new Library();
            }
        }
    }

    public void controlLoop() {
        Options option;

        do {
            printOption();
            option = getOption();
            switch (option) {
                case ADD_BOOK:
                    addBook();
                    break;
                case ADD_MAGAZINE:
                    addMagazine();
                    break;
                case PRINT_BOOKS:
                    printBooks();
                    break;
                case PRINT_MAGAZINES:
                    printMagazines();
                    break;
                case DELETE_BOOK:
                    deleteBook();
                    break;
                case DELETE_MAGAZINE:
                    deleteMagazine();
                    break;
                case ADD_USER:
                    addUser();
                    break;
                case PRINT_USER:
                    printUser();
                    break;
                case EXIT:
                    exit();
                    break;
                case FIND_BOOK:
                    findBook();
                    break;
                default:
                    printer.printLine("Nie ma takiej opcji, wprowadź ponownie:");
                    break;
            }
        } while (option != Options.EXIT);
    }

    private void findBook() {
        printer.printLine("Podaj tytuł publikacji");
        String title = dataReader.getString();
        String notFoundMessage = "Brak publikacji o podanej nazwie";
        library.findPublicationByTitle(title)
                .map(Publication::toString)
                .ifPresentOrElse(System.out::println, () -> System.out.println(notFoundMessage));
    }

    private Options getOption() {
        boolean optionOk = false;
        Options option = null;
        while (!optionOk) {
            try {
                option = Options.createFromInt(dataReader.getInt());
                optionOk = true;
        } catch(NoSuchOptionException e){
            printer.printLine(e.getMessage() + ", podaj ponownie");
        } catch(InputMismatchException ignored){
            printer.printLine("Wprowadzono wartość, która nie jest liczbą, podaj ponownie");
        }
    }
        return option;
}

    private void printOption() {
        printer.printLine("Wybierz opcję");
        for (Options option : Options.values()) {
            System.out.println(option);
        }
    }

    private void addBook() {
        try {
            Book book = dataReader.readAndCreateBook();
            library.addPublication(book);
        } catch (InputMismatchException e) {
            printer.printLine("Nie udało się utworzyć książki, niepoprawne dane");
        } catch (ArrayIndexOutOfBoundsException e) {
            printer.printLine("Osiągnięto limit pojemności, nie można dodać kolejnej książki");
        }
    }

    private void addMagazine() {
        try {
            Magazine magazine = dataReader.readAndCreateMagazine();
            library.addPublication(magazine);
        } catch (InputMismatchException e) {
            printer.printLine("Nie udało się utworzyć magazynu, niepoprawne dane");
        } catch (ArrayIndexOutOfBoundsException e) {
            printer.printLine("Osiągnięto limit pojemności, nie można dodać kolejnego magazynu");
        }
    }

    private void addUser() {
        LibraryUser libUser = dataReader.createLibraryUser();
        try {
            library.addUser(libUser);
        } catch (UserAlreadyExistException e) {
            printer.printLine(e.getMessage());
        }
    }

    private void printBooks() {
        printer.printBooks(library.getSortedPublilcations(
//                (p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle())
                Comparator.comparing(Publication::getTitle, String.CASE_INSENSITIVE_ORDER)
        ));
    }

    private void printMagazines() {
        printer.printMagazines(library.getSortedPublilcations(
//                (p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle())
                Comparator.comparing(Publication::getTitle, String.CASE_INSENSITIVE_ORDER)
        ));
    }

    private void printUser() {
        printer.printUsers(library.getSortedUsers(
//                (p1, p2) -> p1.getLastName().compareToIgnoreCase(p2.getLastName())
                Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
        ));
    }

    private void deleteMagazine() {
        try {
            Magazine magazine = dataReader.readAndCreateMagazine();
            if (library.removePublications(magazine)) {
                printer.printLine("Usunięto magazyn");
            } else {
                printer.printLine("Brak wskazanego magazynu");
            }
        } catch (InputMismatchException e) {
            printer.printLine("Nie udało się utworzyć magazynu, niepoprawne dane");

        }
    }

    private void deleteBook() {
        try {
            Book book = dataReader.readAndCreateBook();
            if (library.removePublications(book)) {
                printer.printLine("Usunięto książkę");
            } else {
                printer.printLine("Brak wskazanej książki");
            }
        } catch (InputMismatchException e) {
            printer.printLine("Nie udało się utworzyć książki, niepoprawne dane");
        }
    }


    private void exit() {
        try {
            fileManager.exportData(library);
            printer.printLine("Eksport danych z pliku zakończony powodzeniem");
        } catch (DataExportException e) {
            printer.printLine(e.getMessage());
        }
        printer.printLine("Koniec programu, papa!");
        dataReader.close();
    }

private enum Options {
    EXIT(0, "Wyjście z programu"),
    ADD_BOOK(1, "Dodanie książki"),
    ADD_MAGAZINE(2, "Dodanie magazynu/gazety"),
    PRINT_BOOKS(3, "Wyświetlenie dostępnych książek"),
    PRINT_MAGAZINES(4, "Wyświetlenie dostępnych magazynów/gazet"),
    DELETE_BOOK(5, "Usuń książkę"),
    DELETE_MAGAZINE(6, "Usuń magazyn"),
    ADD_USER(7, "Dodaj czytelnika"),
    PRINT_USER(8, "Wyświetl czytelników"),
    FIND_BOOK(9, "Wyszukaj książkę");

    private int value;
    private String description;

    Options(int option, String description) {
        this.value = option;
        this.description = description;
    }

    @Override
    public String toString() {
        return value + " - " + description;
    }

    static Options createFromInt(int option) throws NoSuchOptionException {
        try {
            return Options.values()[option];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchOptionException("Brak opcji o id " + option);
        }
    }
}
}
