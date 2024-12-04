package lab8;

import java.sql.*;
import java.util.Scanner;

public class mainApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lab8?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MarcoAsensio#20";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            boolean running = true;
            while (running) {
                System.out.println("\nMeniu:");
                System.out.println("1. Adăugare persoană");
                System.out.println("2. Adăugare excursie");
                System.out.println("3. Afișare persoane și excursii");
                System.out.println("4. Afișare excursii pentru o persoană");
                System.out.println("5. Afișare persoane care au vizitat o destinație");
                System.out.println("6. Afișare persoane care au făcut excursii într-un an");
                System.out.println("7. Ștergere excursie");
                System.out.println("8. Ștergere persoană");
                System.out.println("9. Ieșire");

                int optiune = Integer.parseInt(scanner.nextLine());

                switch (optiune) {
                    case 1 :
                        adaugaPersoana(connection, scanner);
                        break;
                    case 2: adaugaExcursie(connection, scanner);
                    break;
                    case 3:
                        afisarePersoane(connection);
                        afisareExcursii(connection);
                        break;
                    case 4:
                        afisareExcursiiPentruPersoana(connection, scanner);
                        break;
                    case 5:
                        afisarePersoanePentruDestinatie(connection, scanner);
                        break;
                    case 6:
                        afisarePersoanePentruAn(connection, scanner);
                        break;
                    case 7:
                        stergeExcursie(connection, scanner);
                        break;
                    case 8:
                        stergePersoana(connection, scanner);
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adăugare persoană
    private static void adaugaPersoana(Connection connection, Scanner scanner) {
        System.out.print("Introduceți numele persoanei: ");
        String nume = scanner.nextLine();

        try {
            System.out.print("Introduceți vârsta: ");
            int varsta = Integer.parseInt(scanner.nextLine());
            if (varsta <= 0 || varsta > 120) throw new ExceptieVarsta("Vârsta trebuie să fie între 1 și 120.");

            String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nume);
                stmt.setInt(2, varsta);
                stmt.executeUpdate();
                System.out.println("Persoana a fost adăugată cu succes.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Vârsta trebuie să fie un număr valid.");
        } catch (ExceptieVarsta e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adăugare excursie
    private static void adaugaExcursie(Connection connection, Scanner scanner) {
        System.out.print("Introduceți ID-ul persoanei: ");
        int persoanaId = Integer.parseInt(scanner.nextLine());

        try {
            if (!existaPersoana(connection, persoanaId)) {
                System.out.println("Persoana cu ID-ul specificat nu există.");
                return;
            }

            System.out.print("Introduceți destinația: ");
            String destinatie = scanner.nextLine();
            System.out.print("Introduceți anul excursiei: ");
            int an = Integer.parseInt(scanner.nextLine());

            if (an < 1900 || an > java.time.Year.now().getValue())
                throw new ExceptieAnExcursie("Anul excursiei trebuie să fie între 1900 și anul curent.");

            String sql = "INSERT INTO excursii (persoana_id, destinatie, an) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, persoanaId);
                stmt.setString(2, destinatie);
                stmt.setInt(3, an);
                stmt.executeUpdate();
                System.out.println("Excursia a fost adăugată cu succes.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Anul excursiei trebuie să fie un număr valid.");
        } catch (ExceptieAnExcursie e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Verifică existența unei persoane
    private static boolean existaPersoana(Connection connection, int persoanaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM persoane WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, persoanaId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // Afișare persoane
    private static void afisarePersoane(Connection connection) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lab8?serverTimezone=UTC";
        String sql ="select * from persoane";
        Connection connectionn= DriverManager.getConnection(url, "root", "MarcoAsensio#20");
        Statement statement = connectionn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next())
            System.out.println("id=" + rs.getInt("Id") + ", nume= "
                    + rs.getString("nume") + ",varsta=" + rs.getInt("varsta"));

        connectionn.close();
        statement.close();
        rs.close();
    }
    private static void afisareExcursii(Connection connection) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/lab8?serverTimezone=UTC";
        String sql ="select * from excursii";
        Connection connectionn= DriverManager.getConnection(url, "root", "MarcoAsensio#20");
        Statement statement = connectionn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next())
            System.out.println("id excursie=" + rs.getInt("id_excursie") + ", id persoana= "
                    + rs.getInt("id_persoana") +",destinatie="+rs.getString("destinatie")+ ",anul=" + rs.getInt("anul"));

        connectionn.close();
        statement.close();
        rs.close();
    }

    // Afișare excursii pentru o persoană
    private static void afisareExcursiiPentruPersoana(Connection connection, Scanner scanner) {
        System.out.print("Introduceți numele persoanei: ");
        String nume = scanner.nextLine();
        String sql = "SELECT e.destinatie, e.anul " +
                "FROM excursii e " +
                "INNER JOIN persoane p ON e.id_persoana = p.id " +
                "WHERE p.nume = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nume);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Destinație: " + rs.getString("destinatie") + ", An: " + rs.getInt("anul"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Afișare persoane pentru o destinație
    private static void afisarePersoanePentruDestinatie(Connection connection, Scanner scanner) {
        System.out.print("Introduceți destinația: ");
        String destinatie = scanner.nextLine();
        String sql = "SELECT DISTINCT p.nume " +
                "FROM persoane p " +
                "INNER JOIN excursii e ON p.id = e.id_persoana " +
                "WHERE e.destinatie = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, destinatie);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Nume: " + rs.getString("nume"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Afișare persoane pentru un an
    private static void afisarePersoanePentruAn(Connection connection, Scanner scanner) {
        System.out.print("Introduceți anul: ");
        int an = Integer.parseInt(scanner.nextLine());
        String sql = "SELECT DISTINCT p.nume " +
                "FROM persoane p " +
                "INNER JOIN excursii e ON p.id = e.id_persoana " +
                "WHERE e.anul = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, an);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Nume: " + rs.getString("nume"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ștergere excursie
    private static void stergeExcursie(Connection connection, Scanner scanner) {
        System.out.print("Introduceți ID-ul excursiei: ");
        int excursieId = Integer.parseInt(scanner.nextLine());
        String sql = "DELETE FROM excursii WHERE id_excursie = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, excursieId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Excursia a fost ștearsă.");
            } else {
                System.out.println("Excursia nu a fost găsită.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ștergere persoană
    private static void stergePersoana(Connection connection, Scanner scanner) {
        System.out.print("Introduceți ID-ul persoanei: ");
        int persoanaId = Integer.parseInt(scanner.nextLine());
        String deleteExcursii = "DELETE FROM excursii WHERE id_persoana = ?";
        String deletePersoana = "DELETE FROM persoane WHERE id = ?";
        try (PreparedStatement stmt1 = connection.prepareStatement(deleteExcursii);
             PreparedStatement stmt2 = connection.prepareStatement(deletePersoana)) {
            stmt1.setInt(1, persoanaId);
            stmt1.executeUpdate();

            stmt2.setInt(1, persoanaId);
            int rows = stmt2.executeUpdate();
            if (rows > 0) {
                System.out.println("Persoana și excursiile asociate au fost șterse.");
            } else {
                System.out.println("Persoana nu a fost găsită.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
