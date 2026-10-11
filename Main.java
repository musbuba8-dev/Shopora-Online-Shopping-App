import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * SHOPORA - Online Shopping Application
 * A Java Swing mini project demonstrating OOP, ArrayList, file handling,
 * exception handling, searching, cart management, coupons and order history.
 *
 * Run:
 *   javac -encoding UTF-8 src/Main.java
 *   java -cp src Main
 */
public class Main {
    private static final String DATA_DIR = "shopora_data";
    private static final String ORDERS_FILE = DATA_DIR + File.separator + "orders.txt";
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private final List<Product> products = new ArrayList<Product>();
    private final List<CartItem> cart = new ArrayList<CartItem>();
    private final Set<Integer> wishlist = new LinkedHashSet<Integer>();
    private final List<String> orderHistory = new ArrayList<String>();

    private JFrame frame;
    private JPanel root, header, content;
    private JTextField searchField;
    private JComboBox<String> categoryBox;
    private JLabel cartCountLabel, statusLabel;
    private boolean darkMode = false;
    private String activePage = "Home";
    private double budgetLimit = 0;

    private final Color PINK = new Color(225, 91, 137);
    private final Color LIGHT_BG = new Color(250, 247, 245);
    private final Color DARK_BG = new Color(30, 27, 30);
    private final Color CARD_LIGHT = Color.WHITE;
    private final Color TEXT_LIGHT = new Color(45, 42, 44);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main().start();
            }
        });
    }

    private void start() {
        loadProducts();
        loadOrders();
        frame = new JFrame("Shopora | Discover. Shop. Enjoy.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 680));
        frame.setSize(1180, 760);
        frame.setLocationRelativeTo(null);
        buildLayout();
        showHome();
        frame.setVisible(true);
    }

    private void buildLayout() {
        root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT_BG);
        header = new JPanel(new BorderLayout(15, 8));
        header.setBorder(new EmptyBorder(18, 24, 16, 24));
        header.setBackground(new Color(255, 239, 244));

        JPanel brand = new JPanel(new GridLayout(2, 1));
        brand.setOpaque(false);
        JLabel logo = new JLabel("SHOPORA");
        logo.setFont(new Font("SansSerif", Font.BOLD, 27));
        logo.setForeground(PINK);
        JLabel tagline = new JLabel("DISCOVER  •  SHOP  •  ENJOY");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 10));
        tagline.setForeground(new Color(105, 87, 94));
        brand.add(logo);
        brand.add(tagline);
        header.add(brand, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setToolTipText("Search products by name");
        searchField.setBorder(new CompoundBorder(new LineBorder(new Color(235, 205, 216), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JButton searchButton = makeButton("Search", PINK, Color.WHITE);
        searchButton.addActionListener(e -> showProducts());
        searchField.addActionListener(e -> showProducts());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.setPreferredSize(new Dimension(430, 42));
        header.add(searchPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 2));
        actions.setOpaque(false);
        JButton cartButton = makeButton("Cart", new Color(255, 255, 255), PINK);
        cartButton.setBorder(new LineBorder(new Color(235, 205, 216), 1, true));
        cartButton.addActionListener(e -> showCart());
        cartCountLabel = new JLabel("0");
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBackground(PINK);
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cartCountLabel.setPreferredSize(new Dimension(23, 23));
        cartCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton themeButton = makeButton("☾ Theme", new Color(68, 60, 68), Color.WHITE);
        themeButton.addActionListener(e -> {
            darkMode = !darkMode;
            applyTheme();
            navigate(activePage);
        });
        actions.add(cartButton);
        actions.add(cartCountLabel);
        actions.add(themeButton);
        header.add(actions, BorderLayout.EAST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        nav.setBorder(new EmptyBorder(0, 18, 0, 18));
        nav.setBackground(darkMode ? new Color(40, 36, 40) : Color.WHITE);
        String[] navItems = {"Home", "All Products", "Wishlist", "My Orders", "Budget Finder"};
        for (String item : navItems) {
            JButton b = makeButton(item, darkMode ? new Color(60, 54, 60) : Color.WHITE,
                    darkMode ? Color.WHITE : TEXT_LIGHT);
            b.setBorder(new EmptyBorder(8, 13, 8, 13));
            b.addActionListener(e -> navigate(item));
            nav.add(b);
        }
        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.NORTH);
        top.add(nav, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.add(content, BorderLayout.CENTER);

        statusLabel = new JLabel("Welcome to Shopora — your little corner for great finds.");
        statusLabel.setBorder(new EmptyBorder(8, 22, 10, 22));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(120, 110, 115));
        root.add(statusLabel, BorderLayout.SOUTH);
        frame.setContentPane(root);
        applyTheme();
    }

    private void navigate(String page) {
        activePage = page;
        if ("Home".equals(page)) showHome();
        else if ("All Products".equals(page)) showProducts();
        else if ("Wishlist".equals(page)) showWishlist();
        else if ("My Orders".equals(page)) showOrders();
        else if ("Budget Finder".equals(page)) showBudgetFinder();
    }

    private void showHome() {
        activePage = "Home";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);

        JPanel hero = new JPanel(new BorderLayout(10, 10));
        hero.setBackground(darkMode ? new Color(70, 43, 58) : new Color(255, 225, 235));
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel heroText = new JPanel(new GridLayout(3, 1, 0, 4));
        heroText.setOpaque(false);
        JLabel title = new JLabel("A little joy is just a click away.");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(darkMode ? Color.WHITE : new Color(83, 43, 61));
        JLabel subtitle = new JLabel("Explore everyday essentials, cute finds and gifts made for you.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(darkMode ? new Color(245, 225, 235) : new Color(112, 76, 91));
        JButton explore = makeButton("Explore Products  →", PINK, Color.WHITE);
        explore.setPreferredSize(new Dimension(180, 38));
        explore.addActionListener(e -> showProducts());
        heroText.add(title);
        heroText.add(subtitle);
        heroText.add(explore);
        hero.add(heroText, BorderLayout.CENTER);
        JLabel badge = new JLabel("<html><div style='text-align:center;font-size:18px'>♡<br><b>YOUR<br>STYLE</b></div></html>", SwingConstants.CENTER);
        badge.setForeground(PINK);
        badge.setFont(new Font("SansSerif", Font.BOLD, 22));
        badge.setPreferredSize(new Dimension(115, 90));
        hero.add(badge, BorderLayout.EAST);
        page.add(hero, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setOpaque(false);
        JLabel heading = sectionTitle("Popular picks");
        middle.add(heading, BorderLayout.NORTH);
        middle.add(productGrid(getFeaturedProducts()), BorderLayout.CENTER);
        page.add(middle, BorderLayout.CENTER);

        JPanel lower = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        lower.setOpaque(false);
        lower.add(makePill("♡ Wishlist your favourites"));
        lower.add(makePill("✦ Easy coupon savings"));
        lower.add(makePill("✓ Simple, secure demo checkout"));
        page.add(lower, BorderLayout.SOUTH);
        content.add(page, BorderLayout.CENTER);
        refreshCartCount();
        refresh();
    }

    private void showProducts() {
        activePage = "All Products";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(sectionTitle("Find your next favourite"));
        categoryBox = new JComboBox<String>(new String[]{"All Categories", "Fashion", "Electronics", "Beauty", "Home & Living", "Accessories"});
        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryBox.addActionListener(e -> refreshProductGrid(page));
        controls.add(categoryBox);
        page.add(controls, BorderLayout.NORTH);
        refreshProductGrid(page);
        content.add(page, BorderLayout.CENTER);
        refresh();
    }

    private void refreshProductGrid(JPanel page) {
        Component existing = ((BorderLayout) page.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (existing != null) page.remove(existing);
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String category = categoryBox == null ? "All Categories" : (String) categoryBox.getSelectedItem();
        List<Product> filtered = new ArrayList<Product>();
        for (Product p : products) {
            boolean matchesText = p.name.toLowerCase().contains(query) || p.description.toLowerCase().contains(query);
            boolean matchesCategory = category == null || "All Categories".equals(category) || p.category.equals(category);
            boolean matchesBudget = budgetLimit <= 0 || p.price <= budgetLimit;
            if (matchesText && matchesCategory && matchesBudget) filtered.add(p);
        }
        page.add(productGrid(filtered), BorderLayout.CENTER);
        page.revalidate();
        page.repaint();
    }

    private JPanel productGrid(List<Product> list) {
        JPanel grid = new JPanel(new GridLayout(0, 3, 14, 14));
        grid.setOpaque(false);
        if (list.isEmpty()) {
            JLabel empty = new JLabel("No products found. Try a different search or category.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
            grid.add(empty);
            return grid;
        }
        for (Product p : list) grid.add(productCard(p));
        return grid;
    }

    private JPanel productCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(darkMode ? new Color(48, 44, 48) : CARD_LIGHT);
        card.setBorder(new CompoundBorder(new LineBorder(darkMode ? new Color(78, 70, 78) : new Color(237, 226, 230), 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel art = new JLabel("<html><div style='text-align:center;font-size:42px'>" + p.emoji +
                "</div><div style='text-align:center;font-size:11px;'>" + p.category + "</div></html>", SwingConstants.CENTER);
        art.setOpaque(true);
        art.setBackground(darkMode ? new Color(65, 55, 62) : new Color(255, 244, 247));
        art.setPreferredSize(new Dimension(190, 100));
        card.add(art, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 4));
        details.setOpaque(false);
        JLabel name = new JLabel(p.name);
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setForeground(fg());
        JLabel desc = new JLabel("<html><div style='width:200px'>" + p.description + "</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        desc.setForeground(darkMode ? new Color(205, 195, 202) : new Color(120, 110, 115));
        JLabel price = new JLabel("₹" + MONEY.format(p.price) + "     ★ " + p.rating);
        price.setFont(new Font("SansSerif", Font.BOLD, 14));
        price.setForeground(PINK);
        JLabel stock = new JLabel("In stock: " + p.stock);
        stock.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stock.setForeground(fg());
        details.add(name);
        details.add(desc);
        details.add(price);
        details.add(stock);
        card.add(details, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 7, 0));
        buttons.setOpaque(false);
        JButton add = makeButton("Add to Cart", PINK, Color.WHITE);
        add.setFont(new Font("SansSerif", Font.BOLD, 11));
        add.addActionListener(e -> addToCart(p));
        JButton wish = makeButton(wishlist.contains(p.id) ? "♥ Saved" : "♡ Wishlist",
                darkMode ? new Color(70, 60, 68) : new Color(255, 240, 245), PINK);
        wish.setFont(new Font("SansSerif", Font.BOLD, 11));
        wish.addActionListener(e -> toggleWishlist(p));
        buttons.add(add);
        buttons.add(wish);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void showCart() {
        activePage = "Cart";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(15, 15));
        page.setOpaque(false);
        page.add(sectionTitle("Your shopping cart"), BorderLayout.NORTH);
        if (cart.isEmpty()) {
            JLabel empty = new JLabel("<html><div style='text-align:center;font-size:18px'>🛒 Your cart is waiting for something lovely!<br><br><small>Add products from All Products to get started.</small></div></html>", SwingConstants.CENTER);
            empty.setForeground(fg());
            page.add(empty, BorderLayout.CENTER);
        } else {
            String[] columns = {"Product", "Price", "Quantity", "Line Total", "Action"};
            Object[][] data = new Object[cart.size()][5];
            for (int i = 0; i < cart.size(); i++) {
                CartItem ci = cart.get(i);
                data[i][0] = ci.product.name;
                data[i][1] = "₹" + MONEY.format(ci.product.price);
                data[i][2] = ci.quantity;
                data[i][3] = "₹" + MONEY.format(ci.product.price * ci.quantity);
                data[i][4] = "Remove";
            }
            JTable table = new JTable(data, columns);
            table.setRowHeight(30);
            table.setFont(new Font("SansSerif", Font.PLAIN, 13));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(darkMode ? new Color(48, 44, 48) : Color.WHITE);
            page.add(scroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout(10, 10));
            bottom.setOpaque(false);
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            left.setOpaque(false);
            JButton plus = makeButton("+ Quantity", new Color(90, 150, 115), Color.WHITE);
            plus.addActionListener(e -> changeQuantity(table.getSelectedRow(), 1));
            JButton minus = makeButton("− Quantity", new Color(120, 120, 130), Color.WHITE);
            minus.addActionListener(e -> changeQuantity(table.getSelectedRow(), -1));
            JButton remove = makeButton("Remove Item", new Color(185, 70, 85), Color.WHITE);
            remove.addActionListener(e -> removeCartItem(table.getSelectedRow()));
            left.add(plus); left.add(minus); left.add(remove);
            bottom.add(left, BorderLayout.WEST);

            JPanel right = new JPanel(new GridLayout(0, 1, 0, 5));
            right.setOpaque(false);
            JTextField coupon = new JTextField();
            coupon.setColumns(10);
            coupon.setToolTipText("Try SHOP10 for 10% off or WELCOME5 for 5% off");
            JLabel totalLabel = new JLabel("Subtotal: ₹" + MONEY.format(cartTotal()));
            totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            totalLabel.setForeground(fg());
            JLabel discountLabel = new JLabel("Coupon: SHOP10 or WELCOME5");
            discountLabel.setForeground(fg());
            JButton checkout = makeButton("Proceed to Checkout", PINK, Color.WHITE);
            checkout.addActionListener(e -> checkout(coupon.getText().trim()));
            right.add(new JLabel("Coupon code:"));
            right.add(coupon);
            right.add(discountLabel);
            right.add(totalLabel);
            right.add(checkout);
            bottom.add(right, BorderLayout.EAST);
            page.add(bottom, BorderLayout.SOUTH);
        }
        content.add(page, BorderLayout.CENTER);
        refresh();
    }

    private void changeQuantity(int row, int delta) {
        if (row < 0 || row >= cart.size()) {
            message("Select a cart row first.");
            return;
        }
        CartItem ci = cart.get(row);
        if (delta > 0 && ci.quantity >= ci.product.stock) {
            message("Sorry, there is not enough stock.");
            return;
        }
        ci.quantity += delta;
        if (ci.quantity <= 0) cart.remove(row);
        showCart();
        refreshCartCount();
    }

    private void removeCartItem(int row) {
        if (row < 0 || row >= cart.size()) {
            message("Select an item to remove.");
            return;
        }
        cart.remove(row);
        showCart();
        refreshCartCount();
    }

    private void checkout(String coupon) {
        if (cart.isEmpty()) {
            message("Your cart is empty.");
            return;
        }
        double subtotal = cartTotal();
        double rate = 0;
        if ("SHOP10".equalsIgnoreCase(coupon)) rate = 0.10;
        else if ("WELCOME5".equalsIgnoreCase(coupon)) rate = 0.05;
        else if (!coupon.trim().isEmpty()) {
            message("Invalid coupon. Try SHOP10 or WELCOME5.");
            return;
        }
        double discount = subtotal * rate;
        double total = subtotal - discount;

        JTextField customer = new JTextField();
        JTextField address = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Customer name:"));
        form.add(customer);
        form.add(new JLabel("Delivery address:"));
        form.add(address);
        form.add(new JLabel("Subtotal: ₹" + MONEY.format(subtotal)));
        form.add(new JLabel("Discount: ₹" + MONEY.format(discount)));
        form.add(new JLabel("Final total: ₹" + MONEY.format(total)));
        form.add(new JLabel("Demo checkout only — no real payment will be collected."));
        int result = JOptionPane.showConfirmDialog(frame, form, "Checkout", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        if (customer.getText().trim().isEmpty() || address.getText().trim().isEmpty()) {
            message("Please enter your name and delivery address.");
            return;
        }

        String orderId = "SH" + System.currentTimeMillis() % 1000000;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        StringBuilder receipt = new StringBuilder();
        receipt.append("SHOPORA - ORDER RECEIPT\n");
        receipt.append("Order ID: ").append(orderId).append("\nDate: ").append(date).append("\n");
        receipt.append("Customer: ").append(customer.getText().trim()).append("\nAddress: ").append(address.getText().trim()).append("\n\n");
        for (CartItem ci : cart) {
            receipt.append(ci.product.name).append(" x ").append(ci.quantity).append(" = ₹")
                    .append(MONEY.format(ci.product.price * ci.quantity)).append("\n");
        }
        receipt.append("\nSubtotal: ₹").append(MONEY.format(subtotal));
        receipt.append("\nDiscount: ₹").append(MONEY.format(discount));
        receipt.append("\nTOTAL: ₹").append(MONEY.format(total)).append("\n");
        receipt.append("Payment: Demo checkout (not a real payment)\n");

        for (CartItem ci : cart) ci.product.stock -= ci.quantity;
        orderHistory.add(orderId + " | " + date + " | " + customer.getText().trim() + " | ₹" + MONEY.format(total));
        saveOrder(receipt.toString());
        saveProducts();
        cart.clear();
        refreshCartCount();
        JTextArea receiptArea = new JTextArea(receipt.toString(), 18, 42);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(frame, new JScrollPane(receiptArea), "Order placed successfully!", JOptionPane.INFORMATION_MESSAGE);
        saveReceipt(orderId, receipt.toString());
        showHome();
    }

    private void showWishlist() {
        activePage = "Wishlist";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        page.add(sectionTitle("Your wishlist"), BorderLayout.NORTH);
        List<Product> saved = new ArrayList<Product>();
        for (Product p : products) if (wishlist.contains(p.id)) saved.add(p);
        if (saved.isEmpty()) {
            JLabel empty = new JLabel("No saved products yet. Tap ♡ Wishlist on any product you like.", SwingConstants.CENTER);
            empty.setForeground(fg());
            page.add(empty, BorderLayout.CENTER);
        } else page.add(productGrid(saved), BorderLayout.CENTER);
        content.add(page, BorderLayout.CENTER);
        refresh();
    }

    private void showOrders() {
        activePage = "My Orders";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        page.add(sectionTitle("Your order history"), BorderLayout.NORTH);
        if (orderHistory.isEmpty()) {
            JLabel empty = new JLabel("No orders yet. Your completed demo purchases will appear here.", SwingConstants.CENTER);
            empty.setForeground(fg());
            page.add(empty, BorderLayout.CENTER);
        } else {
            DefaultListModel<String> model = new DefaultListModel<String>();
            for (String s : orderHistory) model.addElement(s);
            JList<String> list = new JList<String>(model);
            list.setFont(new Font("SansSerif", Font.PLAIN, 14));
            list.setFixedCellHeight(34);
            page.add(new JScrollPane(list), BorderLayout.CENTER);
        }
        content.add(page, BorderLayout.CENTER);
        refresh();
    }

    private void showBudgetFinder() {
        activePage = "Budget Finder";
        content.removeAll();
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(sectionTitle("Budget-friendly finds"));
        JTextField budgetField = new JTextField("1000", 8);
        JButton find = makeButton("Find Products", PINK, Color.WHITE);
        top.add(new JLabel("My budget (₹):"));
        top.add(budgetField);
        top.add(find);
        page.add(top, BorderLayout.NORTH);
        JPanel results = new JPanel(new BorderLayout());
        results.setOpaque(false);
        JLabel hint = new JLabel("Enter a budget to see products within your price range.", SwingConstants.CENTER);
        hint.setForeground(fg());
        results.add(hint, BorderLayout.CENTER);
        page.add(results, BorderLayout.CENTER);
        find.addActionListener(e -> {
            try {
                double value = Double.parseDouble(budgetField.getText().trim());
                if (value <= 0) throw new NumberFormatException();
                budgetLimit = value;
                List<Product> found = new ArrayList<Product>();
                for (Product p : products) if (p.price <= budgetLimit) found.add(p);
                results.removeAll();
                results.add(productGrid(found), BorderLayout.CENTER);
                results.revalidate();
                results.repaint();
            } catch (NumberFormatException ex) {
                message("Please enter a valid budget greater than zero.");
            }
        });
        content.add(page, BorderLayout.CENTER);
        refresh();
    }

    private List<Product> getFeaturedProducts() {
        List<Product> result = new ArrayList<Product>();
        for (Product p : products) if (p.featured) result.add(p);
        return result;
    }

    private void addToCart(Product p) {
        if (p.stock <= 0) {
            message("This product is currently out of stock.");
            return;
        }
        for (CartItem ci : cart) {
            if (ci.product.id == p.id) {
                if (ci.quantity >= p.stock) {
                    message("You have added all available stock of this product.");
                    return;
                }
                ci.quantity++;
                refreshCartCount();
                message(p.name + " quantity updated in your cart.");
                return;
            }
        }
        cart.add(new CartItem(p, 1));
        refreshCartCount();
        message(p.name + " added to your cart.");
    }

    private void toggleWishlist(Product p) {
        if (wishlist.contains(p.id)) {
            wishlist.remove(p.id);
            message(p.name + " removed from wishlist.");
        } else {
            wishlist.add(p.id);
            message(p.name + " saved to your wishlist.");
        }
        navigate(activePage);
    }

    private double cartTotal() {
        double total = 0;
        for (CartItem ci : cart) total += ci.product.price * ci.quantity;
        return total;
    }

    private void loadProducts() {
        products.clear();
        products.add(new Product(1, "Everyday Tote Bag", "Accessories", 399, "A lightweight bag for daily essentials.", "👜", 4.8, 18, true));
        products.add(new Product(2, "Wireless Headphones", "Electronics", 1499, "Enjoy music, study and calls.", "🎧", 4.7, 12, true));
        products.add(new Product(3, "Classic Wrist Watch", "Accessories", 899, "A simple design for every outfit.", "⌚", 4.5, 15, true));
        products.add(new Product(4, "Soft Glow Lip Tint", "Beauty", 299, "A lovely everyday colour.", "💄", 4.6, 25, true));
        products.add(new Product(5, "Pastel Hoodie", "Fashion", 799, "Comfortable style for casual days.", "🧥", 4.4, 10, false));
        products.add(new Product(6, "Desk Study Lamp", "Home & Living", 649, "A cosy light for your study corner.", "💡", 4.3, 9, false));
        products.add(new Product(7, "Mini Bluetooth Speaker", "Electronics", 1099, "Compact sound for your favourite playlists.", "🔊", 4.5, 14, false));
        products.add(new Product(8, "Daisy Hair Clips", "Beauty", 149, "Cute clips for everyday hairstyles.", "🌼", 4.2, 30, false));
        products.add(new Product(9, "Ceramic Coffee Mug", "Home & Living", 249, "A cheerful mug for coffee or tea.", "☕", 4.6, 20, false));
        products.add(new Product(10, "Minimal Pendant", "Accessories", 499, "A delicate accessory for gifting.", "📿", 4.4, 16, false));
        products.add(new Product(11, "Canvas Sneakers", "Fashion", 1199, "Easy-to-style everyday sneakers.", "👟", 4.5, 8, false));
        products.add(new Product(12, "Pocket Notebook Set", "Home & Living", 199, "Three notebooks for notes and ideas.", "📒", 4.7, 40, false));
        File f = new File(DATA_DIR + File.separator + "products.txt");
        if (!f.exists()) saveProducts();
    }

    private void saveProducts() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();
            PrintWriter out = new PrintWriter(new FileWriter(DATA_DIR + File.separator + "products.txt"));
            for (Product p : products) {
                out.println(p.id + "|" + p.name.replace("|", "/") + "|" + p.category + "|" + p.price + "|" + p.stock);
            }
            out.close();
        } catch (IOException ex) {
            status("Could not save product file: " + ex.getMessage());
        }
    }

    private void loadOrders() {
        File f = new File(ORDERS_FILE);
        if (!f.exists()) return;
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("SUMMARY|")) orderHistory.add(line.substring(8).replace("|", " | "));
            }
            in.close();
        } catch (IOException ex) {
            status("Could not load orders: " + ex.getMessage());
        }
    }

    private void saveOrder(String receipt) {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();
            FileWriter out = new FileWriter(ORDERS_FILE, true);
            String firstLine = receipt.split("\\n")[1];
            String date = receipt.contains("Date: ") ? receipt.split("Date: ")[1].split("\\n")[0] : "";
            String customer = receipt.contains("Customer: ") ? receipt.split("Customer: ")[1].split("\\n")[0] : "";
            String total = receipt.contains("TOTAL: ") ? receipt.split("TOTAL: ")[1].split("\\n")[0] : "";
            out.write("SUMMARY|" + firstLine.replace("Order ID: ", "") + "|" + date + "|" + customer + "|" + total + "\n");
            out.close();
        } catch (IOException ex) {
            status("Could not save order: " + ex.getMessage());
        }
    }

    private void saveReceipt(String orderId, String receipt) {
        try {
            File dir = new File(DATA_DIR + File.separator + "receipts");
            if (!dir.exists()) dir.mkdirs();
            FileWriter out = new FileWriter(new File(dir, "receipt_" + orderId + ".txt"));
            out.write(receipt);
            out.close();
        } catch (IOException ex) {
            status("Could not save receipt: " + ex.getMessage());
        }
    }

    private void refreshCartCount() {
        if (cartCountLabel == null) return;
        int count = 0;
        for (CartItem ci : cart) count += ci.quantity;
        cartCountLabel.setText(String.valueOf(count));
    }

    private void applyTheme() {
        if (root == null) return;
        root.setBackground(darkMode ? DARK_BG : LIGHT_BG);
        header.setBackground(darkMode ? new Color(55, 40, 49) : new Color(255, 239, 244));
        content.setBackground(darkMode ? DARK_BG : LIGHT_BG);
        if (statusLabel != null) {
            statusLabel.setForeground(darkMode ? new Color(195, 185, 192) : new Color(120, 110, 115));
        }
    }

    private void refresh() {
        if (root != null) {
            root.revalidate();
            root.repaint();
        }
    }

    private void message(String text) {
        status(text);
        JOptionPane.showMessageDialog(frame, text, "Shopora", JOptionPane.INFORMATION_MESSAGE);
    }

    private void status(String text) {
        if (statusLabel != null) statusLabel.setText(text);
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(fg());
        return label;
    }

    private JLabel makePill(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(darkMode ? new Color(62, 55, 62) : new Color(255, 255, 255));
        label.setForeground(darkMode ? Color.WHITE : new Color(100, 75, 85));
        label.setBorder(new CompoundBorder(new LineBorder(new Color(238, 215, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(bg.equals(Color.WHITE) ? new Color(230, 215, 222) : bg, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        return b;
    }

    private Color fg() {
        return darkMode ? new Color(245, 238, 242) : TEXT_LIGHT;
    }

    private void refreshProductGridSafe() {
        if ("All Products".equals(activePage)) showProducts();
    }

    // Simple model classes: intentionally kept in this file for easy offline setup.
    private static class Product {
        int id;
        String name, category, description, emoji;
        double price, rating;
        int stock;
        boolean featured;
        Product(int id, String name, String category, double price, String description,
                String emoji, double rating, int stock, boolean featured) {
            this.id=id; this.name=name; this.category=category; this.price=price;
            this.description=description; this.emoji=emoji; this.rating=rating;
            this.stock=stock; this.featured=featured;
        }
    }

    private static class CartItem {
        Product product;
        int quantity;
        CartItem(Product product, int quantity) { this.product=product; this.quantity=quantity; }
    }
}
