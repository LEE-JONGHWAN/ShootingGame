
// ShootingGame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;

public class ShootingGame extends JPanel implements ActionListener, KeyListener {
    Timer timer;
    boolean up, down, left, right;
    boolean isPaused = false;
    Player player;
    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<EnemyMissile> enemyMissiles = new ArrayList<>();

    int score = 0;
    int enemySpawnCounter = 0;
    int enemyMissileCounter = 0;

    Image backgroundImage;
    Image heartImage;

    public ShootingGame() {
        setFocusable(true);
        setPreferredSize(new Dimension(1000, 600));
        setBackground(Color.BLACK);
        addKeyListener(this);

        try {
            backgroundImage = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/background.png"));
            heartImage = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/heart.png"));
        } catch (IOException e) {
            System.out.println("이미지 로딩 실패: " + e.getMessage());
        }

        player = new Player(500, 400);
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            if (up)
                player.move(0, -20);
            if (down)
                player.move(0, 20);
            if (left)
                player.move(-20, 0);
            if (right)
                player.move(20, 0);

            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.move();
                if (bullet.y < 0)
                    bulletIterator.remove();
            }

            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                enemy.move();
                if (enemy.y > getHeight())
                    enemyIterator.remove();
            }

            bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                enemyIterator = enemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy enemy = enemyIterator.next();
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        bulletIterator.remove();
                        enemy.hp--;
                        if (enemy.hp <= 0) {
                            enemyIterator.remove();
                            score += enemy.isSpecial ? 50 : 10;
                        }
                        break;
                    }
                }
            }

            enemyMissileCounter++;
            if (enemyMissileCounter % 40 == 0) {
                for (Enemy enemy : enemies) {
                    enemyMissiles.add(new EnemyMissile(enemy.x + 10, enemy.y + 30));
                }
            }

            Iterator<EnemyMissile> missileIterator = enemyMissiles.iterator();
            while (missileIterator.hasNext()) {
                EnemyMissile em = missileIterator.next();
                em.move();
                if (em.y > getHeight()) {
                    missileIterator.remove();
                } else if (em.getBounds().intersects(player.getBounds())) {
                    missileIterator.remove();
                    player.hp--;
                    if (player.hp <= 0) {
                        JOptionPane.showMessageDialog(this, "Game Over!\nYour score: " + score);
                        System.exit(0);
                    }
                }
            }

            enemySpawnCounter++;
            if (enemySpawnCounter % 50 == 0) {
                int rand = new Random().nextInt(100);
                if (rand < 20) {
                    int specialHp = 3 + new Random().nextInt(3);
                    enemies.add(new Enemy(new Random().nextInt(getWidth() - 30), 0, true, specialHp));
                } else {
                    enemies.add(new Enemy(new Random().nextInt(getWidth() - 30), 0));
                }
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        player.draw(g);
        for (Bullet bullet : bullets)
            bullet.draw(g);
        for (Enemy enemy : enemies)
            enemy.draw(g);
        for (EnemyMissile em : enemyMissiles)
            em.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("SCORE: " + score, 10, 20);

        for (int i = 0; i < player.hp; i++) {
            if (heartImage != null)
                g.drawImage(heartImage, 500 + (i * 30), 10, 25, 25, this);
            else {
                g.setColor(Color.RED);
                g.fillOval(500 + (i * 30), 10, 25, 25);
            }
        }

        if (isPaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", 230, 240);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            up = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)
            down = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)
            left = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT)
            right = true;
        if (code == KeyEvent.VK_K)
            bullets.add(new Bullet(player.x + 10, player.y));
        if (code == KeyEvent.VK_SPACE)
            isPaused = !isPaused;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            up = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)
            down = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)
            left = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT)
            right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Shooting Game");
        ShootingGame game = new ShootingGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static class Player {
        int x, y;
        final int SIZE = 40;
        int hp = 3;
        Image image;

        public Player(int x, int y) {
            this.x = x;
            this.y = y;
            try {
                image = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/cat_player.png"));
            } catch (IOException e) {
                System.out.println("플레이어 이미지 로딩 실패: " + e.getMessage());
            }
        }

        public void move(int dx, int dy) {
            x = Math.max(0, Math.min(1000 - SIZE, x + dx));
            y = Math.max(0, Math.min(600 - SIZE, y + dy));
        }

        public void draw(Graphics g) {
            if (image != null)
                g.drawImage(image, x, y, SIZE, SIZE, null);
            else {
                g.setColor(Color.CYAN);
                g.fillRect(x, y, SIZE, SIZE);
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, SIZE, SIZE);
        }
    }

    static class Bullet {
        int x, y;
        final int SIZE = 40;
        Image image;

        public Bullet(int x, int y) {
            this.x = x;
            this.y = y;
            try {
                image = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/cat_bullet.png"));
            } catch (IOException e) {
                System.out.println("총알 이미지 로딩 실패: " + e.getMessage());
            }
        }

        public void move() {
            y -= 40;
        }

        public void draw(Graphics g) {
            if (image != null)
                g.drawImage(image, x, y, SIZE, SIZE, null);
            else {
                g.setColor(Color.YELLOW);
                g.fillOval(x, y, SIZE, SIZE);
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, SIZE, SIZE);
        }
    }

    static class Enemy {
        int x, y;
        final int SIZE = 50;
        int hp;
        boolean isSpecial;
        Image image;

        public Enemy(int x, int y) {
            this(x, y, false, 1);
        }

        public Enemy(int x, int y, boolean isSpecial, int hp) {
            this.x = x;
            this.y = y;
            this.isSpecial = isSpecial;
            this.hp = hp;
            try {
                image = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/dog_enemy.png"));
            } catch (IOException e) {
                System.out.println("적 이미지 로딩 실패: " + e.getMessage());
            }
        }

        public void move() {
            y += isSpecial ? 1 : 2;
        }

        public void draw(Graphics g) {
            if (image != null)
                g.drawImage(image, x, y, SIZE, SIZE, null);
            else {
                g.setColor(isSpecial ? Color.MAGENTA : Color.RED);
                g.fillRect(x, y, SIZE, SIZE);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("HP:" + hp, x, y - 5);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, SIZE, SIZE);
        }
    }

    static class EnemyMissile {
        int x, y;
        final int SIZE = 10;
        Image image;

        public EnemyMissile(int x, int y) {
            this.x = x;
            this.y = y;
            try {
                image = ImageIO.read(new File("C:/LJH/miniworkspaces/mini2/res/dog_bullet.png")); // C:/LJH/miniworkspaces/mini2/
            } catch (IOException e) {
                System.out.println("적 이미지 로딩 실패: " + e.getMessage());
            }
        }

        public void move() {
            y += 5;
        }

        public void draw(Graphics g) {
            if (image != null)
                g.drawImage(image, x, y, SIZE, SIZE, null);
            else {
                g.setColor(Color.RED);
                g.fillOval(x, y, SIZE, SIZE);
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, SIZE, SIZE);
        }
    }

}
