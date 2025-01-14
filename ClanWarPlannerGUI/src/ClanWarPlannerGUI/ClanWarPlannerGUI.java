package ClanWarPlannerGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ClanWarPlannerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    class Member {
        String name;
        int x, y;

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

    public ClanWarPlannerGUI() {
        // Imposta il font globale a dimensione maggiore
        setUIFont(new Font("Arial", Font.PLAIN, 27)); 
        
        setTitle("Pianificatore Guerra tra Clan");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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
            Color newColor = JColorChooser.showDialog(this, "Scegli il colore della freccia", Color.RED);
            if (newColor != null) {
                plans.forEach(plan -> plan.arrowColor = newColor);
                membersPanel.repaint();
            }
        });

        colorMenu.add(allyColorItem);
        colorMenu.add(enemyColorItem);
        colorMenu.add(arrowColorItem);
        menuBar.add(colorMenu);
        setJMenuBar(menuBar);

        // Inizializzazione pannello grafico
        membersPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setFont(new Font("Arial", Font.PLAIN, 20)); // Dimensione più grande per i nomi

                // Disegna frecce
                for (AttackPlan plan : plans) {
                    g.setColor(plan.arrowColor);
                    g.drawLine(plan.attacker.x, plan.attacker.y, plan.target.x, plan.target.y);
                }

                // Disegna alleati
                g.setColor(allyColor);
                for (Member ally : allies) {
                    g.fillOval(ally.x - 15, ally.y - 15, 30, 30);
                    g.drawString(ally.name, ally.x - 15, ally.y - 20);
                }

                // Disegna nemici
                g.setColor(enemyColor);
                for (Member enemy : enemies) {
                    g.fillRect(enemy.x - 15, enemy.y - 15, 30, 30);
                    g.drawString(enemy.name, enemy.x - 15, enemy.y - 20);
                }
            }
        };

        // Aggiungi la funzionalità di clic per selezionare e collegare membri
        membersPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                final Member[] clickedMember = {null}; // Usando un array per referenziare la variabile

                // Verifica se il clic è su un membro (alleato o nemico)
                for (Member ally : allies) {
                    if (Math.abs(ally.x - mouseX) <= 15 && Math.abs(ally.y - mouseY) <= 15) {
                        clickedMember[0] = ally;
                    }
                }
                for (Member enemy : enemies) {
                    if (Math.abs(enemy.x - mouseX) <= 15 && Math.abs(enemy.y - mouseY) <= 15) {
                        clickedMember[0] = enemy;
                    }
                }

                if (clickedMember[0] != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Gestisci l'attacco
                        if (selectedAlly == null) {
                            selectedAlly = clickedMember[0];  // Seleziona il primo membro
                        } else {
                            // Verifica il limite di 2 attacchi
                            long attackCount = plans.stream()
                                    .filter(plan -> plan.attacker.equals(selectedAlly))
                                    .count();
                            if (attackCount < 2) {
                                plans.add(new AttackPlan(selectedAlly, clickedMember[0], Color.RED));  // Aggiungi la freccia
                                selectedAlly = null;  // Reset selezione
                            } else {
                                JOptionPane.showMessageDialog(ClanWarPlannerGUI.this, "Un alleato può avere solo 2 attacchi.");
                            }
                        }
                        membersPanel.repaint();
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // Rimuovi tutti i collegamenti per il membro cliccato con il tasto destro
                        plans.removeIf(plan -> plan.attacker.equals(clickedMember[0]) || plan.target.equals(clickedMember[0]));
                        membersPanel.repaint();
                    }
                }
            }
        });
        membersPanel.setLayout(null);
        add(membersPanel, BorderLayout.CENTER);

        // Area di visualizzazione
        displayArea = new JTextArea();
        displayArea.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.EAST);

        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton resetButton = new JButton("Ricomincia");
        JButton generateButton = new JButton("Genera Testo Attacchi");

        resetButton.addActionListener(e -> reset());
        generateButton.addActionListener(e -> generateAttackMessages());

        buttonPanel.add(resetButton);
        buttonPanel.add(generateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setupMembers();
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
            addMembers("Alleato", allies, memberCount, 150);
            addMembers("Nemico", enemies, memberCount, 650);
        }
    }

    private void addMembers(String type, ArrayList<Member> list, int count, int x) {
        for (int i = 1; i <= count; i++) {
            String name = JOptionPane.showInputDialog(this, "Inserisci il nome del " + type + " #" + i + ":");
            if (name != null && !name.trim().isEmpty()) {
                int y = i * 50;
                list.add(new Member(name.trim(), x, y));
            }
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
                sb.append("@").append(ally.name).append(": ");
                for (int i = 0; i < 2; i++) {
                    if (i < targets.size()) {
                        sb.append("Attacco ").append(i + 1).append(" -> ").append(" ").append(targets.get(i).name).append("; ");
                    } else {
                        sb.append("Attacco ").append(i + 1).append(" -> Libero; ");
                    }
                }
                sb.append("\n");
            }
        }

        // Creiamo un JTextArea per visualizzare il testo
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        // Impostiamo una dimensione maggiore per il JTextArea
        textArea.setPreferredSize(new Dimension(800, 600));

        // Creiamo un JScrollPane con il JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // Creiamo una finestra di dialogo personalizzata per visualizzare il contenuto
        JDialog dialog = new JDialog(this, "Piani di Attacco", true);
        dialog.setSize(850, 650);  // Imposta le dimensioni della finestra
        dialog.setLocationRelativeTo(this);  // Centra la finestra rispetto alla finestra principale
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Aggiungiamo un pulsante di chiusura alla finestra di dialogo
        JButton closeButton = new JButton("Chiudi");
        closeButton.addActionListener(e -> dialog.dispose());  // Chiude la finestra quando il pulsante viene premuto
        dialog.add(closeButton, BorderLayout.SOUTH);

        dialog.setVisible(true);  // Mostra la finestra di dialogo
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
