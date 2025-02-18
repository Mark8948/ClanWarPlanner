package ClanWarPlannerGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ClanWarPlannerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    class Member {
        String name;
        int x, y; // queste coordinate verranno aggiornate dinamicamente

        Member(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class AttackPlan {
        Member attacker;
        Member target;
        Color arrowColor;

        AttackPlan(Member attacker, Member target, Color arrowColor) {
            this.attacker = attacker;
            this.target = target;
            this.arrowColor = arrowColor;
        }

        @Override
        public String toString() {
            return "@" + attacker.name + ": Attacco -> @" + target.name;
        }
    }

    private final ArrayList<Member> allies = new ArrayList<>();
    private final ArrayList<Member> enemies = new ArrayList<>();
    private final ArrayList<AttackPlan> plans = new ArrayList<>();
    private Member selectedAlly = null;

    private JPanel membersPanel;
    private JTextArea displayArea;
    private Color allyColor = Color.BLUE;
    private Color enemyColor = Color.RED;
    private Color arrowColor = Color.RED; // colore di default per le frecce

    public ClanWarPlannerGUI() {
        // Imposta il font globale a dimensione maggiore
        setUIFont(new Font("Arial", Font.PLAIN, 27));

        setTitle("Pianificatore Guerra tra Clan");
        setSize(1280, 720); // Finestra di base in 1280x720
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Pannello titolo in alto
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("ClanWarPlanner");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Menu per selezionare i colori
        JMenuBar menuBar = new JMenuBar();
        JMenu colorMenu = new JMenu("Colori");
        JMenuItem allyColorItem = new JMenuItem("Colore Alleati");
        JMenuItem enemyColorItem = new JMenuItem("Colore Nemici");
        JMenuItem arrowColorItem = new JMenuItem("Colore Frecce");

        allyColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Scegli il colore degli alleati", allyColor);
            if (newColor != null) {
                allyColor = newColor;
                membersPanel.repaint();
            }
        });

        enemyColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Scegli il colore dei nemici", enemyColor);
            if (newColor != null) {
                enemyColor = newColor;
                membersPanel.repaint();
            }
        });

        arrowColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Scegli il colore della freccia", arrowColor);
            if (newColor != null) {
                arrowColor = newColor;
                plans.forEach(plan -> plan.arrowColor = newColor);
                membersPanel.repaint();
            }
        });

        colorMenu.add(allyColorItem);
        colorMenu.add(enemyColorItem);
        colorMenu.add(arrowColorItem);
        menuBar.add(colorMenu);
        setJMenuBar(menuBar);

        // Inizializzazione del pannello grafico
        membersPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Abilita l'anti-aliasing per testi e grafica più lisci
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Aggiorna dinamicamente le posizioni dei membri in base alle dimensioni attuali
                updateMemberPositions();

                // Calcola la dimensione dell'icona in base all'altezza del pannello
                int iconSize = Math.max(20, getHeight() / 18);
                int halfIcon = iconSize / 2;
                // Calcola la dimensione della freccia (arrowhead)
                int arrowSize = Math.max(6, getHeight() / 72);

                // Disegna le frecce con la nuova funzione
                for (AttackPlan plan : plans) {
                    g2.setColor(plan.arrowColor);
                    drawArrowLine(g2, plan.attacker.x, plan.attacker.y, plan.target.x, plan.target.y, arrowSize * 3, arrowSize);
                }

                // Font per i nomi dei membri (grande e centrato)
                Font memberFont = new Font("Arial", Font.BOLD, 28);
                g2.setFont(memberFont);
                FontMetrics fm = g2.getFontMetrics();

                // Disegna gli alleati come ovali con il nome centrato
                for (Member ally : allies) {
                    g2.setColor(allyColor);
                    g2.fillOval(ally.x - halfIcon, ally.y - halfIcon, iconSize, iconSize);
                    String text = ally.name;
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    g2.setColor(Color.WHITE);
                    g2.drawString(text, ally.x - textWidth / 2, ally.y + textHeight / 2 - 2);
                }

                // Disegna i nemici come rettangoli con il progressivo centrato
                for (Member enemy : enemies) {
                    g2.setColor(enemyColor);
                    g2.fillRect(enemy.x - halfIcon, enemy.y - halfIcon, iconSize, iconSize);
                    String text = enemy.name;
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    g2.setColor(Color.WHITE);
                    g2.drawString(text, enemy.x - textWidth / 2, enemy.y + textHeight / 2 - 2);
                }

                // Evidenzia l'alleato selezionato con un cerchio (leggermente più grande)
                if (selectedAlly != null && allies.contains(selectedAlly)) {
                    g2.setColor(Color.GREEN);
                    g2.drawOval(selectedAlly.x - (halfIcon + 5), selectedAlly.y - (halfIcon + 5), iconSize + 10, iconSize + 10);
                }
            }
        };
        membersPanel.setLayout(null);
        add(membersPanel, BorderLayout.CENTER);

        // Aggiorna le posizioni quando il pannello viene ridimensionato
        membersPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMemberPositions();
                membersPanel.repaint();
            }
        });

        // Area di visualizzazione (non usata per la generazione degli attacchi)
        displayArea = new JTextArea();
        displayArea.setEditable(true);
        JScrollPane scrollPaneDisplay = new JScrollPane(displayArea);
        add(scrollPaneDisplay, BorderLayout.EAST);

        // Pannello pulsanti in basso
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton resetButton = new JButton("Ricomincia");
        JButton generateButton = new JButton("Genera Testo Attacchi");

        resetButton.addActionListener(e -> reset());
        generateButton.addActionListener(e -> generateAttackMessages());

        buttonPanel.add(resetButton);
        buttonPanel.add(generateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listener per la selezione e collegamento dei membri
        membersPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Calcola l'icona corrente (stesso modo usato in paintComponent)
                int iconSize = Math.max(20, membersPanel.getHeight() / 18);
                int halfIcon = iconSize / 2;

                final Member[] clickedMember = {null};

                // Controlla se il clic è su un alleato (area circolare basata su halfIcon)
                for (Member ally : allies) {
                    if (mouseX >= ally.x - halfIcon && mouseX <= ally.x + halfIcon &&
                        mouseY >= ally.y - halfIcon && mouseY <= ally.y + halfIcon) {
                        clickedMember[0] = ally;
                        break; // interrompiamo se abbiamo trovato un match
                    }
                }
                // Se non abbiamo trovato tra gli alleati, controlla tra i nemici (area rettangolare)
                if (clickedMember[0] == null) {
                    for (Member enemy : enemies) {
                        if (mouseX >= enemy.x - halfIcon && mouseX <= enemy.x + halfIcon &&
                            mouseY >= enemy.y - halfIcon && mouseY <= enemy.y + halfIcon) {
                            clickedMember[0] = enemy;
                            break;
                        }
                    }
                }

                if (clickedMember[0] != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Clic sinistro: gestione attacco
                        if (selectedAlly == null) {
                            if (allies.contains(clickedMember[0])) {
                                selectedAlly = clickedMember[0];
                            } else {
                                JOptionPane.showMessageDialog(ClanWarPlannerGUI.this, "Seleziona un alleato come attaccante.");
                            }
                        } else {
                            if (enemies.contains(clickedMember[0])) {
                                long attackCount = plans.stream()
                                        .filter(plan -> plan.attacker.equals(selectedAlly))
                                        .count();
                                if (attackCount < 2) {
                                    plans.add(new AttackPlan(selectedAlly, clickedMember[0], arrowColor));
                                    selectedAlly = null;
                                } else {
                                    JOptionPane.showMessageDialog(ClanWarPlannerGUI.this, "Un alleato può avere solo 2 attacchi.");
                                }
                            } else {
                                JOptionPane.showMessageDialog(ClanWarPlannerGUI.this, "Il bersaglio deve essere un nemico.");
                            }
                        }
                        membersPanel.repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // Clic destro: rimuove tutti i collegamenti per il membro cliccato
                        plans.removeIf(plan -> plan.attacker.equals(clickedMember[0]) || plan.target.equals(clickedMember[0]));
                        if (selectedAlly != null && selectedAlly.equals(clickedMember[0])) {
                            selectedAlly = null;
                        }
                        membersPanel.repaint();
                    }
                }
            }
        });

        setupMembers();
    }

    /**
     * Aggiorna dinamicamente le posizioni dei membri in base alle dimensioni attuali del pannello.
     * Gli alleati vengono posizionati a circa il 25% della larghezza e i nemici al 75%.
     * Le posizioni verticali vengono calcolate in modo uniforme.
     */
    private void updateMemberPositions() {
        int panelWidth = membersPanel.getWidth();
        int panelHeight = membersPanel.getHeight();
        if (panelWidth == 0 || panelHeight == 0) return; // evita divisioni per zero

        // Aggiorna posizioni degli alleati
        int allyCount = allies.size();
        for (int i = 0; i < allyCount; i++) {
            Member ally = allies.get(i);
            ally.x = (int) (panelWidth * 0.25);
            ally.y = (int) ((i + 1) * panelHeight / (allyCount + 1));
        }
        // Aggiorna posizioni dei nemici
        int enemyCount = enemies.size();
        for (int i = 0; i < enemyCount; i++) {
            Member enemy = enemies.get(i);
            enemy.x = (int) (panelWidth * 0.75);
            enemy.y = (int) ((i + 1) * panelHeight / (enemyCount + 1));
        }
    }

    /**
     * Disegna una linea con una freccia da (x1,y1) a (x2,y2).
     * La freccia viene disegnata con una linea spessa e un arrowhead calcolato in base all'angolo.
     *
     * @param g2             Il Graphics2D su cui disegnare.
     * @param x1             Coordinate di partenza.
     * @param y1
     * @param x2             Coordinate di arrivo.
     * @param y2
     * @param arrowHeadLength Lunghezza dell'arrowhead.
     * @param arrowHeadWidth  Larghezza dell'arrowhead.
     */
    private void drawArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2, int arrowHeadLength, int arrowHeadWidth) {
        // Disegna la linea con uno stroke più spesso e con estremità arrotondate
        Stroke originalStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        g2.drawLine(x1, y1, x2, y2);
        g2.setStroke(originalStroke);

        // Calcola l'angolo della linea
        double angle = Math.atan2(y2 - y1, x2 - x1);
        // Calcola le coordinate dei due punti dell'arrowhead
        int xArrow1 = (int) (x2 - arrowHeadLength * Math.cos(angle) + arrowHeadWidth * Math.sin(angle));
        int yArrow1 = (int) (y2 - arrowHeadLength * Math.sin(angle) - arrowHeadWidth * Math.cos(angle));
        int xArrow2 = (int) (x2 - arrowHeadLength * Math.cos(angle) - arrowHeadWidth * Math.sin(angle));
        int yArrow2 = (int) (y2 - arrowHeadLength * Math.sin(angle) + arrowHeadWidth * Math.cos(angle));
        int[] xPoints = { x2, xArrow1, xArrow2 };
        int[] yPoints = { y2, yArrow1, yArrow2 };
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private void setupMembers() {
        String[] options = {"5", "10", "15", "20", "25", "30"};
        String input = (String) JOptionPane.showInputDialog(this,
                "Seleziona il numero di membri della guerra:",
                "Numero membri",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (input != null) {
            int memberCount = Integer.parseInt(input);
            // Aggiungi gli alleati e i nemici; le coordinate iniziali sono impostate a 0
            addMembers("Alleato", allies, memberCount);
            addMembers("Nemico", enemies, memberCount);
            updateMemberPositions();
        }
    }

    private void addMembers(String type, ArrayList<Member> list, int count) {
        for (int i = 1; i <= count; i++) {
            String name;
            if (type.equals("Alleato")) {
                name = JOptionPane.showInputDialog(this, "Inserisci il nome dell'" + type + " #" + i + ":");
                if (name == null || name.trim().isEmpty()) {
                    name = "Alleato" + i;
                }
            } else if (type.equals("Nemico")) {
                // Per i nemici, il nome è il progressivo
                name = String.valueOf(i);
            } else {
                name = "";
            }
            // Le coordinate iniziali saranno aggiornate dinamicamente
            list.add(new Member(name.trim(), 0, 0));
        }
        membersPanel.repaint();
    }

    private void reset() {
        allies.clear();
        enemies.clear();
        plans.clear();
        selectedAlly = null;
        membersPanel.repaint();
        displayArea.setText("");
        setupMembers();
    }

    private void generateAttackMessages() {
        StringBuilder sb = new StringBuilder("--- ATTACCHI IN GUERRA ---\n");
        for (Member ally : allies) {
            ArrayList<Member> targets = new ArrayList<>();
            for (AttackPlan plan : plans) {
                if (plan.attacker.equals(ally)) {
                    targets.add(plan.target);
                }
            }
            if (!targets.isEmpty()) {
                sb.append("@").append(ally.name).append(" -> ");
                sb.append(enemies.indexOf(targets.get(0)) + 1);
                if (targets.size() > 1) {
                    sb.append(",").append(enemies.indexOf(targets.get(1)) + 1);
                }
                sb.append(";  ");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setPreferredSize(new Dimension(800, 600));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JDialog dialog = new JDialog(this, "Piani di Attacco", true);
        dialog.setSize(850, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Chiudi");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private static void setUIFont(Font font) {
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("TitledBorder.font", font);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClanWarPlannerGUI planner = new ClanWarPlannerGUI();
            planner.setVisible(true);
        });
    }
}
