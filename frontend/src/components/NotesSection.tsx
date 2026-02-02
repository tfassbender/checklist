import { useState, useRef, useEffect } from 'react'

interface NotesSectionProps {
  notes: string
  onSave: (notes: string) => void
}

export default function NotesSection({ notes, onSave }: NotesSectionProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isExpanded, setIsExpanded] = useState(!!notes)
  const [text, setText] = useState(notes)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    setText(notes)
  }, [notes])

  useEffect(() => {
    if (isEditing && textareaRef.current) {
      textareaRef.current.focus()
      textareaRef.current.setSelectionRange(
        textareaRef.current.value.length,
        textareaRef.current.value.length
      )
    }
  }, [isEditing])

  const handleSave = () => {
    const trimmed = text.trim()
    onSave(trimmed)
    setIsEditing(false)
    if (!trimmed) {
      setIsExpanded(false)
    }
  }

  const handleCancel = () => {
    setText(notes)
    setIsEditing(false)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      handleCancel()
    } else if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      handleSave()
    }
  }

  if (!isExpanded && !notes) {
    return (
      <button
        onClick={() => {
          setIsExpanded(true)
          setIsEditing(true)
        }}
        className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400
                   hover:text-primary-600 dark:hover:text-primary-400"
      >
        <svg
          className="w-4 h-4"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        Add notes
      </button>
    )
  }

  return (
    <div className="card p-4">
      <div className="flex items-center justify-between mb-2">
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300"
        >
          <svg
            className={`w-4 h-4 transition-transform ${isExpanded ? 'rotate-90' : ''}`}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <polyline points="9 18 15 12 9 6" />
          </svg>
          Notes
        </button>

        {isExpanded && !isEditing && (
          <button
            onClick={() => setIsEditing(true)}
            className="p-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700
                       text-gray-600 dark:text-gray-400"
            title="Edit notes"
          >
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
          </button>
        )}
      </div>

      {isExpanded && (
        <div className="mt-2">
          {isEditing ? (
            <div>
              <textarea
                ref={textareaRef}
                value={text}
                onChange={e => setText(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Add notes for this list..."
                className="input min-h-[100px] resize-y"
                rows={4}
              />
              <div className="flex justify-end gap-2 mt-2">
                <button onClick={handleCancel} className="btn btn-secondary text-sm">
                  Cancel
                </button>
                <button onClick={handleSave} className="btn btn-primary text-sm">
                  Save
                </button>
              </div>
            </div>
          ) : (
            <p className="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">
              {notes || (
                <span className="italic text-gray-400 dark:text-gray-500">
                  No notes added
                </span>
              )}
            </p>
          )}
        </div>
      )}
    </div>
  )
}
