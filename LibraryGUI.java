import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class LibraryGUI extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(15, 23, 42);
    private static final Color BG_CARD       = new Color(30, 41, 59);
    private static final Color BG_SIDEBAR    = new Color(22, 33, 52);
    private static final Color ACCENT        = new Color(99, 179, 237);
    private static final Color ACCENT2       = new Color(72, 187, 120);
    private static final Color ACCENT_WARN   = new Color(237, 137, 54);
    private static final Color ACCENT_DANGER = new Color(245, 101, 101);
    private static final Color TEXT_PRIMARY  = new Color(226, 232, 240);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color BORDER_COLOR  = new Color(51, 65, 85);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  14);
    private static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    // ── Backend ───────────────────────────────────────────────────────────────
    private final LibrarySystem library = new LibrarySystem();

    // ── Navigation ────────────────────────────────────────────────────────────
    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    // ── Status bar ────────────────────────────────────────────────────────────
    private JLabel statusLabel;

    // ── Dashboard live labels ─────────────────────────────────────────────────
    private JLabel dash_totalBooks;
    private JLabel dash_availCopies;
    private JLabel dash_totalUsers;
    private JLabel dash_activeBorrows;

    // ── Books table ───────────────────────────────────────────────────────────
    private DefaultTableModel booksTableModel;

    // ── Borrowers ─────────────────────────────────────────────────────────────
    private DefaultTableModel borrowersTableModel;
    private final List<Borrower> registeredBorrowers = new ArrayList<>();

    // ── My Books ──────────────────────────────────────────────────────────────
    private DefaultTableModel myBooksTableModel;
    private JTextField        myBooksUserField;

    // ── Logs ──────────────────────────────────────────────────────────────────
    private DefaultTableModel borrowLogModel;
    private DefaultTableModel returnLogModel;

    // ── Statistics ────────────────────────────────────────────────────────────
    private DefaultTableModel topBooksModel;
    private DefaultTableModel topAuthorsModel;
    private JLabel            availCountLabel;

    // ─────────────────────────────────────────────────────────────────────────
    public LibraryGUI() {
        super("📚 Library Management System");
        seedDemoData();
        buildFrame();
        setVisible(true);
    }

    // ── Seed ──────────────────────────────────────────────────────────────────
    private void seedDemoData() {
        library.addBook(new Book(9780131103627L, "The C Programming Language", "Kernighan & Ritchie", 4));
        library.addBook(new Book(9780201633610L, "Design Patterns",            "Gang of Four",        3));
        library.addBook(new Book(9780132350884L, "Clean Code",                 "Robert C. Martin",    5));
        library.addBook(new Book(9780596517748L, "JavaScript: The Good Parts", "Douglas Crockford",   2));
        library.addBook(new Book(9781491950357L, "Learning Python",            "Mark Lutz",           3));
        library.addBook(new Book(9780135166307L, "The Pragmatic Programmer",   "Hunt & Thomas",       2));

        Borrower alice = new Borrower("alice", "Alice Johnson",  true);
        Borrower bob   = new Borrower("bob",   "Bob Smith",      false);
        Borrower carol = new Borrower("carol", "Carol Williams", true);
        Borrower dave  = new Borrower("dave",  "Dave Brown",     false);

        for (Borrower b : new Borrower[]{alice, bob, carol, dave}) {
            library.registerBorrower(b);
            registeredBorrowers.add(b);
        }

        library.borrowBook("alice", 9780131103627L);
        library.borrowBook("alice", 9780201633610L);
        library.borrowBook("bob",   9780132350884L);
        library.borrowBook("carol", 9780131103627L);
    }

    // ── Frame ─────────────────────────────────────────────────────────────────
    private void buildFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 740);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildSidebar(), BorderLayout.WEST);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);
        contentPanel.add(buildDashboard(),      "Dashboard");
        contentPanel.add(buildBooksPanel(),     "Books");
        contentPanel.add(buildBorrowersPanel(), "Borrowers");
        contentPanel.add(buildBorrowPanel(),    "Borrow");
        contentPanel.add(buildReturnPanel(),    "Return");
        contentPanel.add(buildMyBooksPanel(),   "My Books");
        contentPanel.add(buildStatsPanel(),     "Statistics");

        root.add(contentPanel,    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
        navigate("Dashboard");
    }

    // ── Central refresh — called after EVERY action ───────────────────────────
    private void refreshAll() {
        refreshDashboard();
        refreshBooksTable();
        refreshBorrowersTable();
        refreshStats();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(BG_SIDEBAR);
        logo.setMaximumSize(new Dimension(205, 72));
        logo.setBorder(new EmptyBorder(20, 20, 20, 16));
        JLabel logoLabel = new JLabel("📚 LibraSys");
        logoLabel.setForeground(ACCENT);
        logoLabel.setFont(FONT_HEADING);
        logo.add(logoLabel);
        sidebar.add(logo);
        sidebar.add(sidebarDivider());

        String[][] items = {
                {"🏠", "Dashboard"},
                {"📖", "Books"},
                {"👥", "Borrowers"},
                {"📤", "Borrow"},
                {"↩",  "Return"},
                {"🗂",  "My Books"},
                {"📊", "Statistics"},
        };
        for (String[] item : items) {
            JButton btn = navButton(item[0] + "  " + item[1], item[1]);
            navButtons.put(item[1], btn);
            sidebar.add(btn);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton navButton(String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(205, 44));
        btn.setPreferredSize(new Dimension(205, 44));
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> navigate(key));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!isActive(key)) btn.setForeground(TEXT_PRIMARY); }
            public void mouseExited(MouseEvent e)  { if (!isActive(key)) btn.setForeground(TEXT_MUTED); }
            private boolean isActive(String k) { return btn.getBackground().equals(BG_CARD); }
        });
        return btn;
    }

    private void navigate(String page) {
        cardLayout.show(contentPanel, page);
        navButtons.forEach((k, b) -> {
            boolean active = k.equals(page);
            b.setForeground(active ? TEXT_PRIMARY : TEXT_MUTED);
            b.setBackground(active ? BG_CARD : BG_SIDEBAR);
        });
    }

    private JSeparator sidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(205, 1));
        return sep;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        statusLabel = new JLabel("  Ready");
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setFont(FONT_SMALL);
        bar.add(statusLabel, BorderLayout.WEST);
        bar.setPreferredSize(new Dimension(0, 26));
        return bar;
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText("  " + msg);
        statusLabel.setForeground(color);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("Dashboard"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(BG_DARK);

        // Create live labels stored as fields
        dash_totalBooks   = bigStatLabel("0", ACCENT);
        dash_availCopies  = bigStatLabel("0", ACCENT2);
        dash_totalUsers   = bigStatLabel("0", ACCENT);
        dash_activeBorrows= bigStatLabel("0", ACCENT_WARN);
        JLabel dashWait   = bigStatLabel("—", TEXT_MUTED);
        JLabel dashOverdue= bigStatLabel("0", ACCENT_DANGER);

        grid.add(statCard("📖  Total Books",       dash_totalBooks));
        grid.add(statCard("✅  Available Copies",  dash_availCopies));
        grid.add(statCard("👥  Registered Users",  dash_totalUsers));
        grid.add(statCard("📤  Active Borrows",    dash_activeBorrows));
        grid.add(statCard("⏳  Waitlists",         dashWait));
        grid.add(statCard("⚠️  Overdue",           dashOverdue));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setBackground(BG_DARK);
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));
        JLabel ql = new JLabel("Quick Actions:");
        ql.setForeground(TEXT_MUTED); ql.setFont(FONT_BODY);
        actions.add(ql);
        actions.add(accentButton("➕ Add Book",      () -> navigate("Books")));
        actions.add(accentButton("👤 Register User", () -> navigate("Borrowers")));
        actions.add(accentButton("📤 Borrow",        () -> navigate("Borrow")));
        actions.add(accentButton("↩ Return",         () -> navigate("Return")));

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG_DARK);
        center.add(grid,    BorderLayout.NORTH);
        center.add(actions, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    /** Push live data into dashboard stat labels. */
    private void refreshDashboard() {
        if (dash_totalBooks == null) return;

        List<Book> allBooks = library.getMostBorrowedBooks(Integer.MAX_VALUE);
        int totalBooks = allBooks.size();
        int availCopies = library.getAvailableCount();
        int totalUsers  = registeredBorrowers.size();
        int activeBorrows = registeredBorrowers.stream()
                .mapToInt(Borrower::getActiveBorrows).sum();

        dash_totalBooks.setText(String.valueOf(totalBooks));
        dash_availCopies.setText(String.valueOf(availCopies));
        dash_totalUsers.setText(String.valueOf(totalUsers));
        dash_activeBorrows.setText(String.valueOf(activeBorrows));
    }

    private JLabel bigStatLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", Font.BOLD, 36));
        return l;
    }

    private JPanel statCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(FONT_SMALL);
        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKS PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("📖 Book Management"), BorderLayout.NORTH);

        // ── Add-book form ──
        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField isbnF   = styledField("e.g. 9780132350884");
        JTextField titleF  = styledField("Book title");
        JTextField authorF = styledField("Author name");
        JTextField copiesF = styledField("Number of copies");

        addFormRow(formCard, gc, 0, "ISBN",   isbnF);
        addFormRow(formCard, gc, 1, "Title",  titleF);
        addFormRow(formCard, gc, 2, "Author", authorF);
        addFormRow(formCard, gc, 3, "Copies", copiesF);

        gc.gridy = 4; gc.gridx = 1; gc.gridwidth = 3;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG_CARD);
        JButton addBtn = accentButton("Add Book", null);
        JButton clrBtn = ghostButton("Clear");
        btnRow.add(addBtn); btnRow.add(clrBtn);
        formCard.add(btnRow, gc);

        // ── Search / delete / update row ──
        gc.gridy = 5; gc.gridx = 0; gc.gridwidth = 4;
        JSeparator sep = new JSeparator(); sep.setForeground(BORDER_COLOR);
        formCard.add(sep, gc);

        JTextField searchF = styledField("ISBN to search / delete / update copies");
        JTextField deltaF  = styledField("±N (update copies)");
        addFormRow(formCard, gc, 6, "ISBN (search/delete/update)", searchF);

        gc.gridy = 6; gc.gridx = 3; gc.gridwidth = 1;
        formCard.add(deltaF, gc);

        gc.gridy = 7; gc.gridx = 1; gc.gridwidth = 3;
        JPanel btn2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btn2.setBackground(BG_CARD);
        JButton searchBtn = accentButton("🔍 Search", null);
        JButton deleteBtn = dangerButton("🗑 Delete");
        JButton updateBtn = warnButton("✏ Update Copies");
        btn2.add(searchBtn); btn2.add(deleteBtn); btn2.add(updateBtn);
        formCard.add(btn2, gc);

        // ── Books table ──
        String[] cols = {"ISBN", "Title", "Author", "Total Copies", "Available", "Times Borrowed"};
        booksTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable booksTable = styledTable(booksTableModel);
        refreshBooksTable();

        // ── Wire actions ──
        addBtn.addActionListener(e -> {
            try {
                long   isbn   = Long.parseLong(isbnF.getText().trim());
                String t      = titleF.getText().trim();
                String a      = authorF.getText().trim();
                int    copies = Integer.parseInt(copiesF.getText().trim());
                if (t.isEmpty() || a.isEmpty()) throw new IllegalArgumentException("Title and Author are required.");
                library.addBook(new Book(isbn, t, a, copies));
                refreshAll();
                setStatus("Book added: " + t, ACCENT2);
                isbnF.setText(""); titleF.setText(""); authorF.setText(""); copiesF.setText("");
            } catch (NumberFormatException ex) { showError("ISBN and Copies must be numbers."); }
            catch (Exception ex)              { showError(ex.getMessage()); }
        });

        clrBtn.addActionListener(e -> {
            isbnF.setText(""); titleF.setText(""); authorF.setText(""); copiesF.setText("");
        });

        searchBtn.addActionListener(e -> {
            try {
                long isbn = Long.parseLong(searchF.getText().trim());
                Book b = library.searchBook(isbn);
                if (b == null) { showError("Book not found."); return; }
                JOptionPane.showMessageDialog(this,
                        "<html><b>ISBN:</b> "      + b.getIsbn()            + "<br>" +
                                "<b>Title:</b> "      + b.getTitle()           + "<br>" +
                                "<b>Author:</b> "     + b.getAuthor()          + "<br>" +
                                "<b>Available:</b> "  + b.getAvailableCopies() + "<br>" +
                                "<b>Borrowed:</b> "   + b.getBorrowCount()     + "</html>",
                        "Book Found", JOptionPane.INFORMATION_MESSAGE);
                setStatus("Found: " + b.getTitle(), ACCENT);
            } catch (NumberFormatException ex) { showError("Enter a valid ISBN."); }
        });

        deleteBtn.addActionListener(e -> {
            try {
                long isbn = Long.parseLong(searchF.getText().trim());
                int  ok   = JOptionPane.showConfirmDialog(this,
                        "Delete book with ISBN " + isbn + "?", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ok == JOptionPane.YES_OPTION) {
                    library.removeBook(isbn);
                    refreshAll();
                    setStatus("Book deleted (ISBN " + isbn + ")", ACCENT_DANGER);
                }
            } catch (NumberFormatException ex) { showError("Enter a valid ISBN."); }
        });

        updateBtn.addActionListener(e -> {
            try {
                long isbn  = Long.parseLong(searchF.getText().trim());
                int  delta = Integer.parseInt(deltaF.getText().trim());
                library.updateCopies(isbn, delta);
                refreshAll();
                setStatus("Copies updated for ISBN " + isbn, ACCENT_WARN);
            } catch (NumberFormatException ex) { showError("Enter valid ISBN and Δ Copies."); }
        });

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(BG_DARK);
        center.add(formCard,                  BorderLayout.NORTH);
        center.add(styledScroll(booksTable),  BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshBooksTable() {
        if (booksTableModel == null) return;
        booksTableModel.setRowCount(0);
        for (Book b : library.getMostBorrowedBooks(Integer.MAX_VALUE)) {
            int total = b.getAvailableCopies() + b.getBorrowCount();
            booksTableModel.addRow(new Object[]{
                    b.getIsbn(), b.getTitle(), b.getAuthor(),
                    total, b.getAvailableCopies(), b.getBorrowCount()
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BORROWERS PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildBorrowersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("👥 Borrower Management"), BorderLayout.NORTH);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField unF   = styledField("Unique username");
        JTextField nameF = styledField("Full name");
        JCheckBox  gradCB = new JCheckBox("Graduate Student (higher borrow priority)");
        gradCB.setForeground(TEXT_PRIMARY);
        gradCB.setBackground(BG_CARD);
        gradCB.setFont(FONT_BODY);

        addFormRow(formCard, gc, 0, "Username",  unF);
        addFormRow(formCard, gc, 1, "Full Name", nameF);
        gc.gridy = 2; gc.gridx = 1; gc.gridwidth = 3;
        formCard.add(gradCB, gc);
        gc.gridy = 3; gc.gridx = 1; gc.gridwidth = 3;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG_CARD);
        JButton regBtn = accentButton("Register Borrower", null);
        JButton clrBtn = ghostButton("Clear");
        btnRow.add(regBtn); btnRow.add(clrBtn);
        formCard.add(btnRow, gc);

        // ── Borrowers table ──
        String[] cols = {"Username", "Full Name", "Graduate", "Active Borrows"};
        borrowersTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(borrowersTableModel);
        refreshBorrowersTable();   // seed from registeredBorrowers

        regBtn.addActionListener(e -> {
            String un   = unF.getText().trim();
            String name = nameF.getText().trim();
            if (un.isEmpty() || name.isEmpty()) { showError("Username and Name required."); return; }
            Borrower b = new Borrower(un, name, gradCB.isSelected());
            Result r   = library.registerBorrower(b);
            if (r.isSuccess()) {
                registeredBorrowers.add(b);
                refreshAll();
                setStatus("Registered: " + name, ACCENT2);
                unF.setText(""); nameF.setText(""); gradCB.setSelected(false);
            } else {
                showError(r.getMessage());
            }
        });
        clrBtn.addActionListener(e -> { unF.setText(""); nameF.setText(""); gradCB.setSelected(false); });

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(BG_DARK);
        center.add(formCard,              BorderLayout.NORTH);
        center.add(styledScroll(table),   BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /** Rebuild borrowers table from live Borrower objects. */
    private void refreshBorrowersTable() {
        if (borrowersTableModel == null) return;
        borrowersTableModel.setRowCount(0);
        for (Borrower b : registeredBorrowers) {
            borrowersTableModel.addRow(new Object[]{
                    b.getBorrowerUserName(),
                    b.getName(),
                    b.isGraduate() ? "Yes" : "No",
                    b.getActiveBorrows()
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BORROW PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("📤 Borrow a Book"), BorderLayout.NORTH);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField unF   = styledField("Borrower username");
        JTextField isbnF = styledField("Book ISBN");
        addFormRow(formCard, gc, 0, "Username", unF);
        addFormRow(formCard, gc, 1, "ISBN",     isbnF);
        gc.gridy = 2; gc.gridx = 1; gc.gridwidth = 3;
        JButton borrowBtn = accentButton("📤 Borrow Book", null);
        formCard.add(borrowBtn, gc);

        JTextArea resultArea = resultBox("Result will appear here…");
        JScrollPane resultScroll = styledScroll(resultArea);

        String[] cols = {"Username", "ISBN", "Date", "Status"};
        borrowLogModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        // seed demo rows
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        borrowLogModel.addRow(new Object[]{"alice", "9780131103627", today, "✅ Borrowed"});
        borrowLogModel.addRow(new Object[]{"alice", "9780201633610", today, "✅ Borrowed"});
        borrowLogModel.addRow(new Object[]{"bob",   "9780132350884", today, "✅ Borrowed"});
        borrowLogModel.addRow(new Object[]{"carol", "9780131103627", today, "✅ Borrowed"});

        JTable logTable = styledTable(borrowLogModel);

        borrowBtn.addActionListener(e -> {
            String un    = unF.getText().trim();
            String isbnS = isbnF.getText().trim();
            if (un.isEmpty() || isbnS.isEmpty()) { showError("Fill in all fields."); return; }
            try {
                long   isbn = Long.parseLong(isbnS);
                Result r    = library.borrowBook(un, isbn);
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                resultArea.setText("[" + date + "]  " + r.getMessage());
                resultArea.setForeground(r.isSuccess() ? ACCENT2 : ACCENT_WARN);
                String status = r.isSuccess() ? "✅ Borrowed" : "⏳ " + r.getMessage();
                borrowLogModel.insertRow(0, new Object[]{un, isbn, date, status});
                refreshAll();
                setStatus(r.getMessage(), r.isSuccess() ? ACCENT2 : ACCENT_WARN);
                unF.setText(""); isbnF.setText("");
            } catch (NumberFormatException ex) { showError("ISBN must be a number."); }
        });

        JSplitPane split = splitPane(
                vBox(formCard, resultScroll),
                labeledScroll("Borrow Log", logTable)
        );
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RETURN PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildReturnPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("↩ Return a Book"), BorderLayout.NORTH);

        JPanel formCard = card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField unF   = styledField("Borrower username");
        JTextField isbnF = styledField("Book ISBN");
        addFormRow(formCard, gc, 0, "Username", unF);
        addFormRow(formCard, gc, 1, "ISBN",     isbnF);
        gc.gridy = 2; gc.gridx = 1; gc.gridwidth = 3;
        JButton returnBtn = accentButton("↩ Return Book", null);
        formCard.add(returnBtn, gc);

        JTextArea resultArea = resultBox("Return result will appear here…");
        JScrollPane resultScroll = styledScroll(resultArea);

        String[] cols = {"Username", "ISBN", "Return Date", "Status"};
        returnLogModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable logTable = styledTable(returnLogModel);

        returnBtn.addActionListener(e -> {
            String un    = unF.getText().trim();
            String isbnS = isbnF.getText().trim();
            if (un.isEmpty() || isbnS.isEmpty()) { showError("Fill in all fields."); return; }
            try {
                long   isbn = Long.parseLong(isbnS);
                Result r    = library.returnBook(un, isbn);
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                resultArea.setText("[" + date + "]  " + r.getMessage());
                resultArea.setForeground(r.isSuccess() ? ACCENT2 : ACCENT_DANGER);
                String status = r.isSuccess()
                        ? (r.getMessage().contains("over due") ? "⚠️ Returned (Overdue)" : "✅ Returned")
                        : "❌ " + r.getMessage();
                returnLogModel.insertRow(0, new Object[]{un, isbn, date, status});
                refreshAll();
                setStatus(r.getMessage(), r.isSuccess() ? ACCENT2 : ACCENT_DANGER);
                unF.setText(""); isbnF.setText("");
            } catch (NumberFormatException ex) { showError("ISBN must be a number."); }
        });

        JSplitPane split = splitPane(
                vBox(formCard, resultScroll),
                labeledScroll("Return Log", logTable)
        );
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MY BOOKS PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildMyBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("🗂 My Books"), BorderLayout.NORTH);

        // ── Lookup form ──
        JPanel formCard = card();
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JLabel lbl = new JLabel("Username:");
        lbl.setForeground(TEXT_MUTED); lbl.setFont(FONT_BODY);
        myBooksUserField = styledField("Enter your username");
        myBooksUserField.setPreferredSize(new Dimension(220, 34));
        JButton lookupBtn = accentButton("View My Books", null);
        formCard.add(lbl);
        formCard.add(myBooksUserField);
        formCard.add(lookupBtn);

        // ── Legend ──
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setBackground(BG_DARK);
        legend.add(legendDot(ACCENT_DANGER, "Overdue"));
        legend.add(legendDot(ACCENT_WARN,   "Due soon (≤3 days)"));
        legend.add(legendDot(ACCENT2,        "On time"));

        // ── Table with overdue highlighting ──
        String[] cols = {"ISBN", "Title", "Author", "Borrow Date", "Due Date", "Status"};
        myBooksTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable myTable = new JTable(myBooksTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String) myBooksTableModel.getValueAt(row, 5);
                if      (status != null && status.startsWith("⚠️")) c.setForeground(ACCENT_DANGER);
                else if (status != null && status.startsWith("🕐")) c.setForeground(ACCENT_WARN);
                else                                                  c.setForeground(ACCENT2);
                c.setBackground(isRowSelected(row) ? new Color(51,65,85) : BG_CARD);
                return c;
            }
        };
        myTable.setBackground(BG_CARD);
        myTable.setFont(FONT_BODY);
        myTable.setRowHeight(32);
        myTable.setGridColor(BORDER_COLOR);
        myTable.setShowVerticalLines(false);
        myTable.getTableHeader().setBackground(BG_SIDEBAR);
        myTable.getTableHeader().setForeground(TEXT_MUTED);
        myTable.getTableHeader().setFont(FONT_SMALL);
        myTable.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER_COLOR));
        myTable.setIntercellSpacing(new Dimension(12, 0));

        JScrollPane tableScroll = styledScroll(myTable);

        // ── Summary label ──
        JLabel summaryLabel = new JLabel(" ");
        summaryLabel.setForeground(TEXT_MUTED);
        summaryLabel.setFont(FONT_SMALL);
        summaryLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        lookupBtn.addActionListener(e -> {
            String un = myBooksUserField.getText().trim();
            if (un.isEmpty()) { showError("Enter a username."); return; }

            // Find borrower in our list
            Borrower found = null;
            for (Borrower b : registeredBorrowers)
                if (b.getBorrowerUserName().equals(un)) { found = b; break; }

            if (found == null) { showError("Borrower '" + un + "' not found."); return; }

            myBooksTableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            int count = 0;

            for (Map.Entry<Long, BorrowRecord> entry : found.getBorrowRecord().entrySet()) {
                BorrowRecord rec = entry.getValue();
                if (rec.isReturned()) continue;   // skip already returned

                Book book = library.searchBook(rec.getIsbn());
                String title  = book != null ? book.getTitle()  : "(Unknown)";
                String author = book != null ? book.getAuthor() : "—";

                long daysLeft = (rec.getExpectedReturn().getTime() - now.getTime())
                        / (1000L * 60 * 60 * 24);

                String status;
                if (rec.isOverDue())      status = "⚠️ Overdue";
                else if (daysLeft <= 3)   status = "🕐 Due in " + daysLeft + " day(s)";
                else                      status = "✅ Due in " + daysLeft + " day(s)";

                myBooksTableModel.addRow(new Object[]{
                        rec.getIsbn(), title, author,
                        sdf.format(rec.getBorrowDate()),
                        sdf.format(rec.getExpectedReturn()),
                        status
                });
                count++;
            }

            if (count == 0) {
                summaryLabel.setText("No active borrows for " + un + ".");
            } else {
                summaryLabel.setText(un + " has " + count + " book(s) currently borrowed.");
            }
            setStatus("Loaded books for: " + un, ACCENT);
        });

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setBackground(BG_DARK);
        top.add(formCard, BorderLayout.NORTH);
        top.add(legend,   BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setBackground(BG_DARK);
        center.add(top,          BorderLayout.NORTH);
        center.add(tableScroll,  BorderLayout.CENTER);
        center.add(summaryLabel, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATISTICS PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        panel.add(pageTitle("📊 Analytics & Statistics"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setBackground(BG_DARK);

        // Available copies card
        JPanel availCard = card();
        availCard.setLayout(new BorderLayout());
        JLabel availTitle = new JLabel("Total Available Copies", SwingConstants.CENTER);
        availTitle.setForeground(TEXT_MUTED); availTitle.setFont(FONT_SMALL);
        availCountLabel = new JLabel("—", SwingConstants.CENTER);
        availCountLabel.setForeground(ACCENT2);
        availCountLabel.setFont(new Font("SansSerif", Font.BOLD, 52));
        availCard.add(availTitle,      BorderLayout.NORTH);
        availCard.add(availCountLabel, BorderLayout.CENTER);
        grid.add(availCard);

        // Top books
        String[] bookCols = {"Rank", "Title", "Borrows"};
        topBooksModel = new DefaultTableModel(bookCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topBooksTable = styledTable(topBooksModel);
        JPanel topBooksCard = card();
        topBooksCard.setLayout(new BorderLayout(0, 8));
        JLabel tbTitle = new JLabel("🔥 Most Borrowed Books");
        tbTitle.setForeground(TEXT_PRIMARY); tbTitle.setFont(FONT_HEADING);
        topBooksCard.add(tbTitle,                    BorderLayout.NORTH);
        topBooksCard.add(styledScroll(topBooksTable), BorderLayout.CENTER);
        grid.add(topBooksCard);

        // Top authors
        String[] authCols = {"Rank", "Author"};
        topAuthorsModel = new DefaultTableModel(authCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topAuthorsTable = styledTable(topAuthorsModel);
        JPanel topAuthCard = card();
        topAuthCard.setLayout(new BorderLayout(0, 8));
        JLabel taTitle = new JLabel("✍️ Top Authors");
        taTitle.setForeground(TEXT_PRIMARY); taTitle.setFont(FONT_HEADING);
        topAuthCard.add(taTitle,                      BorderLayout.NORTH);
        topAuthCard.add(styledScroll(topAuthorsTable), BorderLayout.CENTER);
        grid.add(topAuthCard);

        JButton refreshBtn = ghostButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshStats());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(BG_DARK);
        btnRow.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(BG_DARK);
        center.add(btnRow, BorderLayout.NORTH);
        center.add(grid,   BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStats() {
        if (availCountLabel == null) return;
        availCountLabel.setText(String.valueOf(library.getAvailableCount()));
        topBooksModel.setRowCount(0);
        List<Book> top = library.getMostBorrowedBooks(5);
        for (int i = 0; i < top.size(); i++)
            topBooksModel.addRow(new Object[]{"#"+(i+1), top.get(i).getTitle(), top.get(i).getBorrowCount()});
        topAuthorsModel.setRowCount(0);
        List<String> authors = library.getTopAuthors(5);
        for (int i = 0; i < authors.size(); i++)
            topAuthorsModel.addRow(new Object[]{"#"+(i+1), authors.get(i)});
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE); l.setForeground(TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 18, 0));
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED); l.setFont(FONT_BODY);
        return l;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_MUTED); g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, 8, getHeight()/2+5);
                }
            }
        };
        f.setBackground(BG_DARK); f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);  f.setFont(FONT_BODY);
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JTextArea resultBox(String hint) {
        JTextArea ta = new JTextArea(4, 40);
        ta.setEditable(false); ta.setFont(FONT_MONO);
        ta.setBackground(BG_DARK); ta.setForeground(TEXT_PRIMARY);
        ta.setBorder(new EmptyBorder(8,8,8,8));
        ta.setText(hint);
        return ta;
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridy = row; gc.gridx = 0; gc.gridwidth = 1; gc.weightx = 0;
        p.add(label(lbl+":"), gc);
        gc.gridx = 1; gc.gridwidth = 3; gc.weightx = 1;
        p.add(field, gc);
    }

    private JButton accentButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY); btn.setBackground(ACCENT); btn.setForeground(BG_DARK);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8,18,8,18));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY); btn.setBackground(BG_CARD); btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR,1,true), new EmptyBorder(7,16,7,16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY); btn.setBackground(ACCENT_DANGER); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8,18,8,18));
        return btn;
    }

    private JButton warnButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY); btn.setBackground(ACCENT_WARN); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8,18,8,18));
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY); t.setFont(FONT_BODY);
        t.setRowHeight(32); t.setGridColor(BORDER_COLOR);
        t.setSelectionBackground(new Color(51,65,85)); t.setSelectionForeground(TEXT_PRIMARY);
        t.setShowVerticalLines(false);
        t.getTableHeader().setBackground(BG_SIDEBAR); t.getTableHeader().setForeground(TEXT_MUTED);
        t.getTableHeader().setFont(FONT_SMALL);
        t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER_COLOR));
        t.setIntercellSpacing(new Dimension(12,0));
        return t;
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER_COLOR,1));
        sp.getViewport().setBackground(BG_CARD); sp.setBackground(BG_CARD);
        return sp;
    }

    private JSplitPane splitPane(JComponent top, JComponent bottom) {
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        sp.setResizeWeight(0.45); sp.setBorder(null); sp.setBackground(BG_DARK);
        return sp;
    }

    private JPanel vBox(JComponent... components) {
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setBackground(BG_DARK);
        if (components.length > 0) p.add(components[0], BorderLayout.NORTH);
        if (components.length > 1) p.add(components[1], BorderLayout.CENTER);
        return p;
    }

    private JPanel labeledScroll(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout(0,4));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel(title);
        lbl.setForeground(TEXT_MUTED); lbl.setFont(FONT_SMALL);
        lbl.setBorder(new EmptyBorder(8,0,4,0));
        p.add(lbl,              BorderLayout.NORTH);
        p.add(styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel legendDot(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(BG_DARK);
        JLabel dot = new JLabel("●");
        dot.setForeground(color); dot.setFont(FONT_SMALL);
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_MUTED); lbl.setFont(FONT_SMALL);
        p.add(dot); p.add(lbl);
        return p;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("Error: " + msg, ACCENT_DANGER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(LibraryGUI::new);
    }
}