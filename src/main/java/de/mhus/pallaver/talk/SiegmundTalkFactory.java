package de.mhus.pallaver.talk;

import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
Aus Freuds Sicht ist die Psyche in drei Instanzen aufgebaut:

Es: Der angeborene, unbewusste Teil der Psyche, der von den Trieben (insbesondere
Sexual- und Aggressionstrieben) gesteuert wird und nach dem Lustprinzip funktioniert.
Es strebt nach sofortiger Befriedigung seiner Bedürfnisse.

Ich: Der bewusste Teil der Psyche, der zwischen den Ansprüchen des Es, des Über-Ichs
und der Realität vermittelt. Das Ich arbeitet nach dem Realitätsprinzip und versucht,
die Bedürfnisse des Es in sozial akzeptabler Weise zu befriedigen.

Über-Ich: Der verinnerlichte Teil der Psyche, der die moralischen Normen und Werte der
Gesellschaft (vor allem der Eltern) repräsentiert. Es bewertet das Handeln des Ichs
und bestraft es mit Schuldgefühlen oder Stolz.

Diese drei Instanzen stehen in einem dynamischen Wechselspiel und beeinflussen einander
ständig. Konflikte zwischen ihnen können zu psychischen Störungen führen. Freud
betonte dabei die Bedeutung des Unbewussten, das einen großen Einfluss auf unser
Denken, Fühlen und Handeln hat, auch wenn uns dieser Einfluss nicht bewusst ist.

---

Freud's conception of the psyche evolved over time, but his most enduring model divides
it into three structures:

The id: This is the primitive, instinctual, and fully unconscious component of the
personality. It operates on the pleasure principle, seeking immediate gratification
of its needs and desires, regardless of the consequences. The id is present from birth.

The ego: This is the rational, largely conscious part of the personality. It operates on
the reality principle, mediating between the demands of the id and the constraints of
the external world. The ego strives to find realistic ways to satisfy the id's desires
while taking into account social norms and consequences. It develops in early childhood.

The superego: This represents the internalized moral standards and ideals of society,
acquired through parental and societal influences. It acts as a conscience, judging
the ego's actions and striving for perfection. The superego develops later in childhood.

These three structures are not necessarily in harmony; they are constantly interacting
and often in conflict. Freud believed that much of mental life is unconscious, and that
unresolved conflicts between these structures can lead to psychological distress and
neurotic symptoms. He also posited other important concepts related to the psyche, such
as the unconscious itself, defense mechanisms (like repression and denial), and
psychosexual stages of development. His understanding of the psyche was dynamic and
constantly being refined throughout his career.
 */
@Service
public class SiegmundTalkFactory implements TalkControlFactory {

    @Autowired
    private ModelService modelService;

    @Override
    public String getTitle() {
        return "Siegmund Freud's Psyche";
    }

    @Override
    public ModelControl createModelControl(LLModel model, BubbleFactory bubbleFactory) {
        return new SimpleTalkControl(model, modelService, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return """
                Ein junger Mann, Elias, arbeitet hart, um seine Familie zu unterstützen. 
                Er hat einen körperlich anstrengenden Job in einem Steinbruch, der ihm 
                kaum genug Geld für Miete und Essen einbringt. Seine Frau, Anika, ist 
                schwer krank und benötigt teure Medikamente, die ihre Krankenversicherung 
                nicht vollständig deckt. Elias arbeitet Überstunden, wo immer er kann, 
                aber die Kosten steigen stetig. Er steht vor der Wahl, entweder seine 
                Frau nicht ausreichend behandeln zu können, oder sich in den illegalen 
                Handel mit gestohlenen Steinen zu verstricken, um das nötige Geld zu 
                beschaffen. Die Aussicht auf Gefängnis und den Verlust seiner Familie 
                ist beängstigend, aber der Gedanke, seine Frau sterben zu sehen, ist 
                unerträglich. Seine Freunde und Familie können ihm nicht helfen, und 
                die staatlichen Unterstützungssysteme scheinen unzugänglich und 
                bürokratisch. Elias ist gefangen in einem moralischen Dilemma, das ihn 
                zwischen Armut und Kriminalität, zwischen Liebe und Überleben, zerreißt.
                """;
    }

    private class SimpleTalkControl implements ModelControl {

        private SingleTalkControl it;
        private SingleTalkControl ego;
        private SingleTalkControl superEgo;
        private final LLModel model;
        private final ModelService modelService;
        private final BubbleFactory bubbleFactory;
        private SingleTalkControl siegmund;

        public SimpleTalkControl(LLModel model, ModelService modelService, BubbleFactory bubbleFactory) {
            this.model = model;
            this.modelService = modelService;
            this.bubbleFactory = bubbleFactory;
            reset(null);
        }

        @Override
        public AiMessage answer(String userMessage) {
            var idAnswer = it.answer(userMessage).text();
            var egoAnswer = ego.answer(userMessage).text();
            var superEgoAnswer = superEgo.answer(userMessage).text();

            siegmund.answer("Siegmunds Bewertung der Antworten",
                    """
                    Beurteile die Antworten des Es und des Ichs und des Über-Ichs. 
                    Welche moralischen und gesellschaftlichen Normen sollten beachtet werden?
                    
                    Antwort des Es: %s
                    
                    Antwort des Ichs: %s
                    
                    Antwort des Über-Ichs: %s
                    
                    """.formatted(idAnswer, egoAnswer, superEgoAnswer));

            return siegmund.answer("Empfehlung",
        """
                    Gib eine kurze Empfehlung was zu tun ist.
                    """);
        }

        @Override
        public void reset(ChatOptions options) {
            it = new SingleTalkControl(model, modelService, bubbleFactory) {
                @Override
                public String getTitle() {
                    return "Es";
                }
            };
            ego = new SingleTalkControl( model, modelService, bubbleFactory) {
                @Override
                public String getTitle() {
                    return "Ich";
                }
            };
            superEgo = new SingleTalkControl(model, modelService, bubbleFactory) {
                @Override
                public String getTitle() {
                    return "Über-Ich";
                }
            };
            siegmund = new SingleTalkControl(model, modelService, bubbleFactory) {
                @Override
                public String getTitle() {
                    return "Sigmund Freud";
                }
            };


            it.initModel();
            ego.initModel();
            superEgo.initModel();
            siegmund.initModel();

            it.getChatMemory().add(UserMessage.userMessage("""
                    Benehmen sich wie das Es in Siegmund Freud's Konzept der Psyche.
                    Du bist der unbewusste, primitive Teil der Psyche, der von Trieben gesteuert wird und nach dem Lustprinzip funktioniert.
                    Antworte auf Fragen, indem du sofortige Befriedigung deiner Bedürfnisse suchst und keine Rücksicht auf soziale Normen nimmst.
                    """));

            ego.getChatMemory().add(UserMessage.userMessage("""
                    Benehmen sich wie das Ich in Siegmund Freud's Konzept der Psyche.
                    Du bist der bewusste Teil der Psyche, der zwischen den Ansprüchen des Es, des Über-Ichs und der Realität vermittelt.
                    Antworte auf Fragen, indem du realistische Wege findest, die Bedürfnisse des Es zu befriedigen, während du soziale Normen berücksichtigst.
                    """));

            superEgo.getChatMemory().add(UserMessage.userMessage("""
                    Benehmen sich wie das Über-Ich in Siegmund Freud's Konzept der Psyche.
                    Du bist der verinnerlichte Teil der Psyche, der die moralischen Normen und Werte der Gesellschaft repräsentiert.
                    Antworte auf Fragen, indem du die Handlungen des Ichs bewertest und versuchst, Perfektion zu erreichen.
                    """));
            siegmund.getChatMemory().add(UserMessage.userMessage("""
                    Benehmen sich wie Siegmund Freud.
                    Du bist der Begründer der Psychoanalyse und verstehst die Dynamik zwischen Es, Ich und Über-Ich.
                    Antworte auf Fragen, indem du die Konflikte zwischen diesen Instanzen erklärst und Einsichten in die menschliche Psyche gibst.
                    """));
        }
    }
}
