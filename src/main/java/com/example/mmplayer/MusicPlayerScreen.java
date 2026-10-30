package com.example.mmplayer;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;


public class MusicPlayerScreen extends Screen {

    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_COLOR = 0xFF404040;
    private static final int ROW_COLOR = 0x99202020;
    private static final int ROW_HOVER_COLOR = 0xFF3A3A45;
    private static final int ROW_PLAYING_COLOR = 0xFF1D4A20;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int TAG_COLOR = 0xFF808080;
    private static final int PLAYING_TAG_COLOR = 0xFF55FF55;

    private final Screen parent;

    private EditBox search;
    private List<MusicLibrary.Track> tracks;
    private String lastQuery = "";
    private double scrollOffset;
    private boolean draggingScrollBar;

    private int listX;
    private int listTop;
    private int listBottom;
    private int listRight;

    public MusicPlayerScreen(Screen parent) {
        super(Component.literal("zabyt's music player"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.tracks = MusicLibrary.filter("");
        int centerX = this.width / 2;
        this.listX = centerX - 130;
        this.listRight = centerX + 130;
        this.listTop = 44;
        this.listBottom = this.height - 56;

        this.search = new EditBox(this.font, centerX - 100, 20, 200, 18, Component.literal("Search"));
        this.search.setHint(Component.literal("Search music..."));
        this.search.setResponder(query -> {
            this.tracks = MusicLibrary.filter(query);
            clampScroll();
        });
        this.addRenderableWidget(this.search);

        this.addRenderableWidget(Button.builder(Component.literal("Stop"), btn -> MusicPlayer.stop(this.minecraft))
                .bounds(this.listX, this.listBottom + 24, 40, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(this.listRight - 60, this.height - 27, 60, 20)
                .build());

        this.setInitialFocus(this.search);
    }

    @Override
    public void tick() {
        MusicPlayer.tick(this.minecraft);
        String query = this.search.getValue();
        if (!query.equals(this.lastQuery)) {
            this.lastQuery = query;
            this.tracks = MusicLibrary.filter(query);
            clampScroll();
        }
    }

    private void clampScroll() {
        int rows = rowsPerPage();
        int max = Math.max(0, this.tracks.size() - rows);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, max);
    }

    private int rowsPerPage() {
        return Math.max(1, (this.listBottom - this.listTop) / ROW_HEIGHT);
    }

    private boolean showScrollBar() {
        return this.tracks.size() > rowsPerPage();
    }

    private int maxScrollOffset() {
        return Math.max(0, this.tracks.size() - rowsPerPage());
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    private int firstRowOffset() {
        return Mth.floor(this.scrollOffset);
    }

    private MusicLibrary.Track rowAt(double mouseX, double mouseY) {
        if (mouseX < this.listX || mouseX > this.listRight || mouseY < this.listTop || mouseY >= this.listBottom) {
            return null;
        }
        int row = (int) ((mouseY - this.listTop) / ROW_HEIGHT) + firstRowOffset();
        if (row >= 0 && row < this.tracks.size()) {
            return this.tracks.get(row);
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mx = event.x();
        double my = event.y();

        if (showScrollBar() && mx >= this.listRight + 2 && mx <= this.listRight + 9
                && my >= this.listTop && my <= this.listBottom) {
            this.draggingScrollBar = true;
        }

        boolean overRow = rowAt(mx, my) != null;
        boolean superHandled = super.mouseClicked(event, doubled);

        if (overRow) {
            MusicLibrary.Track track = rowAt(mx, my);
            this.draggingScrollBar = false;
            if (track != null) {
                if (MusicPlayer.isPlaying(track.fileId())) {
                    MusicPlayer.stop(this.minecraft);
                } else {
                    MusicPlayer.play(this.minecraft, track.fileId());
                }
            }
            return true;
        }

        if (!superHandled) {
            this.setFocused(null);
        }
        return superHandled;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingScrollBar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.draggingScrollBar && showScrollBar()) {
            double trackHeight = this.listBottom - this.listTop;
            double ratio = (event.y() - this.listTop) / trackHeight;
            this.scrollOffset = Mth.clamp(ratio * maxScrollOffset(), 0, maxScrollOffset());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double target = Mth.clamp(this.scrollOffset - scrollY * 2, 0, maxScrollOffset());
        if (target == this.scrollOffset) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        this.scrollOffset = target;
        this.draggingScrollBar = false;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.search.isFocused() && MMPlayerClient.OPEN_MUSIC_PLAYER.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFF);


        graphics.fill(this.listX - 2, this.listTop - 2, this.listRight + 2, this.listBottom + 2, HEADER_COLOR);
        graphics.fill(this.listX, this.listTop, this.listRight, this.listBottom, 0xFF000000);

        int first = firstRowOffset();
        for (int i = 0; i < rowsPerPage(); i++) {
            int index = first + i;
            if (index >= this.tracks.size()) {
                break;
            }
            MusicLibrary.Track track = this.tracks.get(index);
            int x = this.listX;
            int y = this.listTop + i * ROW_HEIGHT;

            boolean playing = MusicPlayer.isPlaying(track.fileId());
            boolean hovered = rowAt(mouseX, mouseY) == track && mouseY >= y && mouseY < y + ROW_HEIGHT;
            int bg = playing ? ROW_PLAYING_COLOR : hovered ? ROW_HOVER_COLOR : ROW_COLOR;
            graphics.fill(x, y, this.listRight, y + ROW_HEIGHT - 1, bg);

            String prefix = playing ? "\u25B6 " : "";
            graphics.text(this.font, prefix + track.title(), x + 4, y + 4, playing ? PLAYING_TAG_COLOR : NAME_COLOR, true);

            String tag = track.artist().isEmpty() ? track.category() : track.artist();
            int tagWidth = this.font.width(tag);
            graphics.text(this.font, tag, this.listRight - tagWidth - 4, y + 4, playing ? PLAYING_TAG_COLOR : TAG_COLOR, true);
        }

        if (this.tracks.isEmpty()) {
            graphics.centeredText(this.font, Component.literal("No matching tracks"), (this.listX + this.listRight) / 2,
                    (this.listTop + this.listBottom) / 2 - 4, TAG_COLOR);
        }


        if (showScrollBar()) {
            int barX = this.listRight + 2;
            int trackHeight = this.listBottom - this.listTop;
            double thumbRatio = (double) rowsPerPage() / this.tracks.size();
            int thumbHeight = Math.max(10, (int) (trackHeight * thumbRatio));
            double scrollRatio = maxScrollOffset() == 0 ? 0 : this.scrollOffset / maxScrollOffset();
            int thumbY = this.listTop + (int) ((trackHeight - thumbHeight) * scrollRatio);
            graphics.fill(barX, thumbY, barX + 7, thumbY + thumbHeight, 0xFF808080);
        }


        Identifier nowPlaying = MusicPlayer.currentId();
        int bottomY = this.height - 50;
        graphics.fill(this.listX - 2, bottomY, this.listRight + 2, bottomY + 14, 0x99000000);
        String now = nowPlaying == null ? "Nothing playing" : "Now playing: " + nameOf(nowPlaying);
        graphics.text(this.font, now, this.listX + 2, bottomY + 3, nowPlaying == null ? TAG_COLOR : PLAYING_TAG_COLOR, true);
        int soundCount = this.tracks.size();
        String count = soundCount + " track" + (soundCount == 1 ? "" : "s");
        graphics.text(this.font, count, this.listRight - this.font.width(count) - 2, bottomY + 3, TAG_COLOR, true);
    }

    private static String nameOf(Identifier id) {
        for (MusicLibrary.Track track : MusicLibrary.all()) {
            if (track.fileId().equals(id)) {
                return track.title();
            }
        }
        return id.getPath();
    }
}

